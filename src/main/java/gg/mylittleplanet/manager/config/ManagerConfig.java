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
    private GitConfig git;
    
    @Nullable
    private NetworkConfig network;
    
    @NotNull
    private InfrastructureConfig infrastructure = new InfrastructureConfig();
    
    @Nullable
    private ServerGroup proxy;
    
    @Nullable
    private ServerGroup gameservers;
    
    @Data
    public static class ServerGroup {
        @Nullable
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