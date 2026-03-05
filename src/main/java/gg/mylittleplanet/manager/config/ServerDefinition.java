package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServerDefinition {
    @Nullable
    private String id;
    
    @Nullable
    private String type;
    
    @Nullable
    private Integer fixedPort;
    
    @Nullable
    private String gitPullUrl;       // git repo containing this server's files
    
    @Nullable
    private ResourcesConfig resources;
    
    private Map<String, String> env = new HashMap<>();
}