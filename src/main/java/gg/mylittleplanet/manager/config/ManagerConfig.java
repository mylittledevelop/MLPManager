package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "manager")
public class ManagerConfig {
    private PanelConfig panel;
    private NodeDefinitionConfig node;
    private EggConfig egg;
    private GitConfig git;
    private NetworkConfig network;
    private InfrastructureConfig infrastructure = new InfrastructureConfig();
    private List<ServerDefinition> servers = new ArrayList<>();
}