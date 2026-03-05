package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EggDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("startup")
    private String startup;

    @JsonProperty("variables")
    private List<EggVariable> variables;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EggVariable {
        @JsonProperty("name")
        private String name;

        @JsonProperty("env_variable")
        private String envVariable;

        @JsonProperty("rules")
        private String rules;

        @JsonProperty("description")
        private String description;

        // Optional: rules might be a comma-separated list of allowed values (e.g., "in:proxy,lobby,planets")
    }
}