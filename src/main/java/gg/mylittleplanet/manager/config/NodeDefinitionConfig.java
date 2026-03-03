package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class NodeDefinitionConfig {
    private String name;
    private String locationName;
    private String fqdn;
    private PortRange portRange;
    private ResourcesConfig resources;

    @Data
    public static class PortRange {
        private int from;
        private int to;
    }
}