package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

@Data
public class NodeDefinitionConfig {
    @Nullable
    private String name;
    
    @Nullable
    private String locationName;
    
    @Nullable
    private String fqdn;
    
    @Nullable
    private PortRange portRange;
    
    @Nullable
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