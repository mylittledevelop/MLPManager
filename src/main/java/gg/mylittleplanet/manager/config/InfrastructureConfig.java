package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class InfrastructureConfig {
    @Nullable
    private String wingsContainerName = "mlpinfra-wings-1";

    @Nullable
    private String wingsConfigPath = "/etc/pterodactyl/config.yml";

    // If true, restart and health-check the Wings container after writing config.
    private boolean manageWingsContainer = true;

    // Max seconds to wait for `docker restart` to finish.
    private int wingsRestartTimeoutSeconds = 30;

    // Max seconds to wait for Wings container readiness after restart.
    private int wingsReadyTimeoutSeconds = 60;

    // Poll interval (seconds) for readiness checks.
    private int wingsReadyPollIntervalSeconds = 2;
}