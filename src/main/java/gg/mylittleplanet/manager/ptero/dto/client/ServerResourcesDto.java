package gg.mylittleplanet.manager.ptero.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerResourcesDto {
    @JsonProperty("current_state")
    private String currentState;    // "running", "offline", "starting", "stopping"

    @JsonProperty("is_suspended")
    private boolean suspended;

    @JsonProperty("resources")
    private Resources resources;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resources {
        @JsonProperty("memory_bytes")
        private long memoryBytes;

        @JsonProperty("cpu_absolute")
        private double cpuAbsolute;

        @JsonProperty("disk_bytes")
        private long diskBytes;

        @JsonProperty("uptime")
        private long uptime;
    }
}