package gg.mylittleplanet.manager.ptero.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PteroListWrapper<T> {
    @JsonProperty("object")
    private String object;

    @JsonProperty("data")
    private List<PteroDataWrapper<T>> data;
}