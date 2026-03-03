package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class InfrastructureConfig {
    private String wingsContainerName = "mlpinfra-wings-1";
    private String wingsConfigPath = "/etc/pterodactyl/config.yml";
}