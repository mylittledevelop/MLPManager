package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "manager")
public class ManagerConfig {
    @Nullable
    private PanelConfig panel;
    
    @Nullable
    private NodeDefinitionConfig node;
    
    @Nullable
    private EggConfig egg;
    
    @Nullable
    private GitConfig git;
    
    @Nullable
    private NetworkConfig network;
    
    @NotNull
    private InfrastructureConfig infrastructure = new InfrastructureConfig();
    
    @NotNull
    private List<ServerDefinition> servers = new ArrayList<>();
}