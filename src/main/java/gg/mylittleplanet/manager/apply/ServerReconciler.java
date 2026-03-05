package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.EggConfig;
import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.config.ServerDefinition;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerReconciler {

    private final PteroAppClient appClient;
    private final ManagerConfig config;

    public void reconcile(int nodeId, ApplyResult result) {
        // Fetch everything we need upfront
        final int adminUserId = findAdminUserId(result);

        final List<ServerDto> existingServers = appClient.listServers();
        final Map<String, ServerDto> existingByName = existingServers.stream()
                .collect(Collectors.toMap(ServerDto::getName, s -> s));

        final List<AllocationDto> allAllocations = appClient.listAllocations(nodeId);
        // Track allocations claimed during this reconcile run
        final Set<Integer> claimedAllocationIds = allAllocations.stream()
                .filter(AllocationDto::isAssigned)
                .map(AllocationDto::getId)
                .collect(Collectors.toCollection(HashSet::new));

        // Reconcile proxy servers
        if (config.getProxy() != null && config.getProxy().getEgg() != null) {
            final EggDto proxyEgg = appClient.getEgg(
                    config.getProxy().getEgg().getNestId(), 
                    config.getProxy().getEgg().getId()
            );
            result.info("Using proxy egg '" + proxyEgg.getName() + "' with startup: " + proxyEgg.getStartup());
            logEggVariables(proxyEgg, result);

            for (final ServerDefinition server : config.getProxy().getServers()) {
                try {
                    reconcileServer(nodeId, server, existingByName, allAllocations,
                            claimedAllocationIds, adminUserId, proxyEgg, config.getProxy().getEgg(), result);
                } catch (Exception e) {
                    result.error("Failed to reconcile proxy server '" + server.getId() + "': " + e.getMessage());
                    log.error("Proxy server reconcile failed for {}", server.getId(), e);
                }
            }
        }

        // Reconcile game servers
        if (config.getGameservers() != null && config.getGameservers().getEgg() != null) {
            final EggDto gameEgg = appClient.getEgg(
                    config.getGameservers().getEgg().getNestId(), 
                    config.getGameservers().getEgg().getId()
            );
            result.info("Using game server egg '" + gameEgg.getName() + "' with startup: " + gameEgg.getStartup());
            logEggVariables(gameEgg, result);

            for (final ServerDefinition server : config.getGameservers().getServers()) {
                try {
                    reconcileServer(nodeId, server, existingByName, allAllocations,
                            claimedAllocationIds, adminUserId, gameEgg, config.getGameservers().getEgg(), result);
                } catch (Exception e) {
                    result.error("Failed to reconcile game server '" + server.getId() + "': " + e.getMessage());
                    log.error("Game server reconcile failed for {}", server.getId(), e);
                }
            }
        }
    }

    private void logEggVariables(EggDto egg, ApplyResult result) {
        if (egg.getVariables() != null && !egg.getVariables().isEmpty()) {
            result.info("Egg variables:");
            for (final EggDto.EggVariable var : egg.getVariables()) {
                result.info("  - " + var.getEnvVariable() + " (rules: " + var.getRules() + ")");
            }
        }
    }

    private void reconcileServer(
            int nodeId,
            @NotNull ServerDefinition server,
            @NotNull Map<String, ServerDto> existingByName,
            @NotNull List<AllocationDto> allAllocations,
            @NotNull Set<Integer> claimedAllocationIds,
            int adminUserId,
            @NotNull EggDto egg,
            @NotNull EggConfig eggConfig,
            @NotNull ApplyResult result
    ) {
        // Validate server ID
        if (server.getId() == null || server.getId().isBlank()) {
            result.error("Server ID cannot be empty");
            return;
        }

        if (existingByName.containsKey(server.getId())) {
            result.skipped("Server '" + server.getId() + "' already exists");
            // Drift detection can be added here later
            return;
        }

        final AllocationDto allocation = findAllocation(server, allAllocations, claimedAllocationIds);
        claimedAllocationIds.add(allocation.getId());

        final Map<String, String> env = buildEnvironment(server, eggConfig);

        appClient.createServer(CreateServerRequest.builder()
                .name(server.getId())
                .userId(adminUserId)
                .nodeId(nodeId)
                .eggId(eggConfig.getId())
                .dockerImage(eggConfig.getDockerImage())
                .startup(egg.getStartup())
                .environment(env)
                .limits(CreateServerRequest.Limits.builder()
                        .memory(server.getResources().getMemoryMb())
                        .swap(0)
                        .disk(server.getResources().getDiskMb())
                        .io(500)
                        .cpu(server.getResources().getCpu())
                        .oomDisabled(false)
                        .build()
                )
                .featureLimits(CreateServerRequest.FeatureLimits.builder()
                        .databases(0)
                        .allocations(0)
                        .backups(0)
                        .build())
                .allocation(CreateServerRequest.AllocationRequest.of(allocation.getId()))
                .build());

        result.created("Server '" + server.getId() + "' created on port " + allocation.getPort());
    }

    private @NotNull AllocationDto findAllocation(
            @NotNull ServerDefinition server,
            @NotNull List<AllocationDto> allocations,
            @NotNull Set<Integer> claimedIds
    ) {
        if (server.getFixedPort() != null) {
            return allocations.stream()
                    .filter(a -> a.getPort() == server.getFixedPort())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No allocation found for fixed port " + server.getFixedPort()));
        }

        return allocations.stream()
                .filter(a -> !claimedIds.contains(a.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No free allocation available for server '" + server.getId() + "'"));
    }

    private @NotNull Map<String, String> buildEnvironment(@NotNull ServerDefinition server, @NotNull EggConfig eggConfig) {
        final Map<String, String> env = new LinkedHashMap<>();

        // ── Git script (global) ────────────────────────────────────────────
        env.put("GIT_SCRIPT_URL", config.getGit().getScriptUrl());
        env.put("GIT_SCRIPT_BRANCH", config.getGit().getScriptBranch());
        env.put("SERVER_SCRIPT_USERNAME", config.getGit().getScriptUser());
        env.put("SERVER_SCRIPT_ACCESS_TOKEN", config.getGit().getPullToken());

        // ── Git pull (per server) ──────────────────────────────────────────
        // Only include GIT_PULL_URL if it's explicitly configured
        if (server.getGitPullUrl() != null && !server.getGitPullUrl().isBlank()) {
            env.put("GIT_PULL_URL", server.getGitPullUrl());
        }
        env.put("GIT_USERNAME", config.getGit().getPullUser());
        env.put("GIT_ACCESS_TOKEN", config.getGit().getPullToken());

        // ── Egg variables (from egg config) ───────────────────────────────
        env.put("SERVER_JARFILE", eggConfig.getServerJarfile());
        env.put("BUILD_DIR", eggConfig.getBuildDir());
        env.put("BUILD_NUMBER", eggConfig.getBuildNumber());
        env.put("START_SCRIPT", eggConfig.getStartScript());

        // ── Per server ─────────────────────────────────────────────────────
        env.put("SERVER_ID", server.getId());

        if (server.getType() != null && !server.getType().isBlank()) 
            env.put("SERVER_TYPE", server.getType());

        // ── Per-server env overrides from config — always last ─────────────
        env.putAll(server.getEnv());

        return env;
    }

    private int findAdminUserId(@NotNull ApplyResult result) {
        return appClient.listUsers().stream()
                .filter(UserDto::isRootAdmin)
                .findFirst()
                .map(user -> {
                    result.info("Using admin user '" + user.getUsername() + "' (id=" + user.getId() + ")");
                    return user.getId();
                })
                .orElseThrow(() -> new RuntimeException("No admin user found in Pterodactyl"));
    }
}