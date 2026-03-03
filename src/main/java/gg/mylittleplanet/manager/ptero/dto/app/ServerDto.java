package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;          // null = normal, "installing", "suspended" etc.

    @JsonProperty("node")
    private int node;

    @JsonProperty("allocation")
    private int allocation;

    @JsonProperty("limits")
    private Limits limits;

    @JsonProperty("container")
    private Container container;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Limits {
        @JsonProperty("memory")
        private int memory;

        @JsonProperty("disk")
        private int disk;

        @JsonProperty("cpu")
        private int cpu;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Container {
        @JsonProperty("startup_command")
        private String startupCommand;

        @JsonProperty("environment")
        private Map<String, String> environment;

        @JsonProperty("image")
        private String image;
    }
}