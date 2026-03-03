package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CreateServerRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("user")
    private int userId;

    @JsonProperty("egg")
    private int eggId;

    @JsonProperty("docker_image")
    private String dockerImage;

    @JsonProperty("startup")
    private String startup;

    @JsonProperty("environment")
    private Map<String, String> environment;

    @JsonProperty("limits")
    private Limits limits;

    @JsonProperty("feature_limits")
    private FeatureLimits featureLimits;

    @JsonProperty("allocation")
    private AllocationRequest allocation;

    @Data
    @Builder
    public static class Limits {
        @JsonProperty("memory")
        private int memory;

        @JsonProperty("disk")
        private int disk;

        @JsonProperty("cpu")
        private int cpu;
    }

    @Data
    @Builder
    public static class FeatureLimits {
        @JsonProperty("databases")
        private int databases;

        @JsonProperty("backups")
        private int backups;
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class AllocationRequest {
        @JsonProperty("default")
        private int defaultAllocation;
    }
}