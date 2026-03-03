package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.config.ServerDefinition;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        final EggDto egg = appClient.getEgg(config.getEgg().getNestId(), config.getEgg().getId());
        result.info("Using egg '" + egg.getName() + "' with startup: " + egg.getStartup());

        final List<ServerDto> existingServers = appClient.listServers();
        final Map<String, ServerDto> existingByName = existingServers.stream()
                .collect(Collectors.toMap(ServerDto::getName, s -> s));

        final List<AllocationDto> allAllocations = appClient.listAllocations(nodeId);
        // Track allocations claimed during this reconcile run
        final Set<Integer> claimedAllocationIds = allAllocations.stream()
                .filter(AllocationDto::isAssigned)
                .map(AllocationDto::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (final ServerDefinition server : config.getServers()) {
            try {
                reconcileServer(server, existingByName, allAllocations,
                        claimedAllocationIds, adminUserId, egg, result);
            } catch (Exception e) {
                result.error("Failed to reconcile server '" + server.getId() + "': " + e.getMessage());
                log.error("Server reconcile failed for {}", server.getId(), e);
            }
        }
    }

    private void reconcileServer(
            ServerDefinition server,
            Map<String, ServerDto> existingByName,
            List<AllocationDto> allAllocations,
            Set<Integer> claimedAllocationIds,
            int adminUserId,
            EggDto egg,
            ApplyResult result
    ) {
        if (existingByName.containsKey(server.getId())) {
            result.skipped("Server '" + server.getId() + "' already exists");
            // Drift detection can be added here later
            return;
        }

        final AllocationDto allocation = findAllocation(server, allAllocations, claimedAllocationIds);
        claimedAllocationIds.add(allocation.getId());

        final Map<String, String> env = buildEnvironment(server);

        appClient.createServer(CreateServerRequest.builder()
                .name(server.getId())
                .userId(adminUserId)
                .eggId(config.getEgg().getId())
                .dockerImage(config.getEgg().getDockerImage())
                .startup(egg.getStartup())
                .environment(env)
                .limits(CreateServerRequest.Limits.builder()
                        .memory(server.getResources().getMemoryMb())
                        .disk(server.getResources().getDiskMb())
                        .cpu(server.getResources().getCpu())
                        .build())
                .featureLimits(CreateServerRequest.FeatureLimits.builder()
                        .databases(0)
                        .backups(0)
                        .build())
                .allocation(CreateServerRequest.AllocationRequest.of(allocation.getId()))
                .build());

        result.created("Server '" + server.getId() + "' created on port " + allocation.getPort());
    }

    private AllocationDto findAllocation(
            ServerDefinition server,
            List<AllocationDto> allocations,
            Set<Integer> claimedIds
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

    private Map<String, String> buildEnvironment(ServerDefinition server) {
        final Map<String, String> env = new LinkedHashMap<>();

        // Git credentials from global git config
        env.put("GIT_USERNAME", config.getGit().getPullUser());
        env.put("GIT_ACCESS_TOKEN", config.getGit().getPullToken());
        env.put("SERVER_SCRIPT_REPO", config.getGit().getScriptUrl());
        env.put("SERVER_SCRIPT_USERNAME", config.getGit().getScriptUser());
        env.put("SERVER_SCRIPT_ACCESS_TOKEN", config.getGit().getPullToken());
        env.put("SERVER_SCRIPT_BRANCH", config.getGit().getScriptBranch());
        env.put("SERVER_ID", server.getId());

        // Per-server env overrides from config — these take priority
        env.putAll(server.getEnv());

        return env;
    }

    private int findAdminUserId(ApplyResult result) {
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