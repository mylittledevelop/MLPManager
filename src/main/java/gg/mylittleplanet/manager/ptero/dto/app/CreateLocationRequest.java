package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateLocationRequest {
    @JsonProperty("short")
    private String shortCode;

    @JsonProperty("long")
    private String description;
}