package gg.mylittleplanet.manager.ptero.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendCommandRequest {
    @JsonProperty("command")
    private String command;
}