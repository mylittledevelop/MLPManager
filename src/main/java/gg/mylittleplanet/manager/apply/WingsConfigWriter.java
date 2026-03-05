package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class WingsConfigWriter {

    private final PteroAppClient appClient;
    private final ManagerConfig config;

    public void writeAndRestart(int nodeId, ApplyResult result) {
        if (!isRunningInDocker()) {
            result.info("Skipping Wings config write — not running inside Docker (local dev mode)");
            return;
        }

        final Path wingsConfigPath = Path.of(config.getInfrastructure().getWingsConfigPath());

        try {
            final String wingsConfig = appClient.getNodeConfiguration(nodeId);
            Files.createDirectories(wingsConfigPath.getParent());
            Files.writeString(wingsConfigPath, wingsConfig);
            result.info("Wings config written to " + wingsConfigPath);
        } catch (IOException e) {
            result.error("Failed to write Wings config: " + e.getMessage());
            throw new RuntimeException("Wings config write failed", e);
        }

        restartWings(result);
        waitForWings(result);
    }

    private void restartWings(@NotNull ApplyResult result) {
        final String containerName = config.getInfrastructure().getWingsContainerName();
        try {
            result.info("Restarting Wings container '" + containerName + "'...");
            final Process process = new ProcessBuilder("docker", "restart", containerName)
                    .redirectErrorStream(true)
                    .start();
            final int exitCode = process.waitFor();
            if (exitCode == 0) {
                result.info("Wings container restarted successfully");
            } else {
                result.error("Wings restart exited with code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            result.error("Failed to restart Wings: " + e.getMessage());
            throw new RuntimeException("Wings restart failed", e);
        }
    }

    private void waitForWings(@NotNull ApplyResult result) {
        result.info("Waiting 20 seconds for Wings to come online...");
        try {
            Thread.sleep(20_000);
            result.info("Wings should be online");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.error("Wait for Wings was interrupted");
            throw new RuntimeException("Wings startup wait interrupted", e);
        }
    }

    // Detect if we're running inside a Docker container
    private boolean isRunningInDocker() {
        return Files.exists(Path.of("/.dockerenv"));
    }
}