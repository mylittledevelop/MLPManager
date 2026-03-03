package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateAllocationRequest {
    @JsonProperty("ip")
    private String ip;

    @JsonProperty("ports")
    private List<String> ports;     // e.g. ["25565", "25566-25600"]
}