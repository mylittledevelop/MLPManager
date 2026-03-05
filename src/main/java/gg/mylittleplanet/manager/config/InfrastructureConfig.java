package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class InfrastructureConfig {
    @Nullable
    private String wingsContainerName = "mlpinfra-wings-1";
    
    @Nullable
    private String wingsConfigPath = "/etc/pterodactyl/config.yml";
}