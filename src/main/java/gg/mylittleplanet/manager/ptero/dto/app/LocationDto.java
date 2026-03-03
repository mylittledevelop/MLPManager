package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("short")
    private String shortCode;

    @JsonProperty("long")
    private String description;
}