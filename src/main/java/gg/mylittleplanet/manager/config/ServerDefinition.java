package gg.mylittleplanet.manager.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServerDefinition {
    private String id;
    private String type;
    private Integer fixedPort;       // null = pick from pool automatically
    private ResourcesConfig resources;
    private Map<String, String> env = new HashMap<>();
}