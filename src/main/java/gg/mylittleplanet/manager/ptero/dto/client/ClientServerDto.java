package gg.mylittleplanet.manager.ptero.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientServerDto {
    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("is_suspended")
    private boolean suspended;

    @JsonProperty("is_installing")
    private boolean installing;
}