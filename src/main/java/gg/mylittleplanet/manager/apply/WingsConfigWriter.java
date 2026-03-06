package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.InfrastructureConfig;
import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WingsConfigWriter {

    private final PteroAppClient appClient;
    private final ManagerConfig config;

    public void writeAndRestart(int nodeId, ApplyResult result) {
        if (!isRunningInDocker()) {
            result.info("Skipping Wings config write - not running inside Docker (local dev mode)");
            return;
        }

        final InfrastructureConfig infra = config.getInfrastructure();
        final String configPathValue = Objects.requireNonNull(
                infra.getWingsConfigPath(),
                "manager.infrastructure.wings-config-path must not be null"
        );
        final Path wingsConfigPath = Path.of(configPathValue);

        try {
            final String wingsConfig = appClient.getNodeConfiguration(nodeId);
            Files.createDirectories(wingsConfigPath.getParent());
            Files.writeString(wingsConfigPath, wingsConfig);
            result.info("Wings config written to " + wingsConfigPath);
        } catch (IOException e) {
            result.error("Failed to write Wings config: " + e.getMessage());
            throw new RuntimeException("Wings config write failed", e);
        }

        if (!infra.isManageWingsContainer()) {
            result.info("Skipping Wings container restart (manager.infrastructure.manage-wings-container=false)");
            return;
        }

        restartWings(result, infra);
        waitForWings(result, infra);
    }

    private void restartWings(@NotNull ApplyResult result, @NotNull InfrastructureConfig infra) {
        final String containerName = Objects.requireNonNull(
                infra.getWingsContainerName(),
                "manager.infrastructure.wings-container-name must not be null"
        );
        result.info("Restarting Wings container '" + containerName + "'...");

        final CommandResult restart = runDockerCommand(
                new String[]{"docker", "restart", containerName},
                infra.getWingsRestartTimeoutSeconds()
        );

        if (restart.timedOut()) {
            result.error("Wings restart timed out after " + infra.getWingsRestartTimeoutSeconds() + " seconds");
            throw new RuntimeException("Wings restart timed out");
        }

        if (restart.exitCode() != 0) {
            final String details = restart.output().isBlank() ? "no output" : restart.output();
            result.error("Wings restart failed (exit " + restart.exitCode() + "): " + details);
            throw new RuntimeException("Wings restart failed: " + details);
        }

        result.info("Wings container restarted successfully");
    }

    private void waitForWings(@NotNull ApplyResult result, @NotNull InfrastructureConfig infra) {
        final String containerName = Objects.requireNonNull(
                infra.getWingsContainerName(),
                "manager.infrastructure.wings-container-name must not be null"
        );
        final int timeoutSeconds = Math.max(1, infra.getWingsReadyTimeoutSeconds());
        final int pollSeconds = Math.max(1, infra.getWingsReadyPollIntervalSeconds());
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);

        result.info("Waiting for Wings container health (timeout " + timeoutSeconds + "s)...");

        String lastState = "unknown";
        while (System.nanoTime() < deadline) {
            final CommandResult inspect = runDockerCommand(
                    new String[]{"docker", "inspect", "--format", "{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}", containerName},
                    pollSeconds + 2
            );

            if (!inspect.timedOut() && inspect.exitCode() == 0) {
                final String state = inspect.output();
                if (!state.isBlank()) {
                    lastState = state;
                }

                final String[] parts = state.split("\\|", 2);
                final String status = parts.length > 0 ? parts[0].trim() : "";
                final String health = parts.length > 1 ? parts[1].trim() : "none";

                if ("running".equalsIgnoreCase(status)
                        && ("healthy".equalsIgnoreCase(health) || "none".equalsIgnoreCase(health))) {
                    result.info("Wings is ready (state=" + status + ", health=" + health + ")");
                    return;
                }
            } else if (inspect.timedOut()) {
                lastState = "inspect timeout";
            } else if (!inspect.output().isBlank()) {
                lastState = inspect.output();
            }

            sleepSeconds(pollSeconds);
        }

        result.error("Wings did not become ready in time (last state: " + lastState + ")");
        throw new RuntimeException("Wings startup validation failed");
    }

    private @NotNull CommandResult runDockerCommand(@NotNull String[] command, int timeoutSeconds) {
        try {
            final Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            final boolean finished = process.waitFor(Math.max(1, timeoutSeconds), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandResult(-1, "timed out", true);
            }

            final int exitCode = process.exitValue();
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return new CommandResult(exitCode, output, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while executing Docker command", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute Docker command", e);
        }
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(Math.max(1, seconds) * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Wings readiness", e);
        }
    }

    // Detect if we're running inside a Docker container
    private boolean isRunningInDocker() {
        return Files.exists(Path.of("/.dockerenv"));
    }

    private record CommandResult(int exitCode, @NotNull String output, boolean timedOut) {}
}
