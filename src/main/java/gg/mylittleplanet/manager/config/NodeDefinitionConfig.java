package gg.mylittleplanet.manager.config;

import lombok.Data;

import java.util.stream.IntStream;

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

        public IntStream portStream() {
            return IntStream.rangeClosed(from, to);
        }
    }
}