package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UpdateStartupRequest {
    @JsonProperty("startup")
    private String startup;

    @JsonProperty("environment")
    private Map<String, String> environment;

    @JsonProperty("egg")
    private int eggId;

    @JsonProperty("image")
    private String image;

    @JsonProperty("skip_scripts")
    private boolean skipScripts;
}