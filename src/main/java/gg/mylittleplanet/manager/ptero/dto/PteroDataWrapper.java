package gg.mylittleplanet.manager.ptero.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PteroDataWrapper<T> {
    @JsonProperty("object")
    private String object;

    @JsonProperty("attributes")
    private T attributes;
}