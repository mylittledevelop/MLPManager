package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "manager")
public class ManagerConfig {
    @Nullable
    @NestedConfigurationProperty
    private PanelConfig panel;
    
    @Nullable
    @NestedConfigurationProperty
    private NodeDefinitionConfig node;
    
    @Nullable
    @NestedConfigurationProperty
    private GitConfig git;
    
    @Nullable
    @NestedConfigurationProperty
    private NetworkConfig network;
    
    @NotNull
    @NestedConfigurationProperty
    private InfrastructureConfig infrastructure = new InfrastructureConfig();
    
    @Nullable
    @NestedConfigurationProperty
    private ServerGroup proxy;
    
    @Nullable
    @NestedConfigurationProperty
    private ServerGroup gameservers;
    
    @Data
    public static class ServerGroup {
        @Nullable
        @NestedConfigurationProperty
        private EggConfig egg;
        
        @NotNull
        private List<ServerDefinition> servers = new ArrayList<>();
    }
    
    public @NotNull List<@NotNull ServerDefinition> getServers() {
        final List<@NotNull ServerDefinition> all = new ArrayList<>();
        if (proxy != null) {
            all.addAll(proxy.getServers());
        }
        if (gameservers != null) {
            all.addAll(gameservers.getServers());
        }
        return all;
    }
}