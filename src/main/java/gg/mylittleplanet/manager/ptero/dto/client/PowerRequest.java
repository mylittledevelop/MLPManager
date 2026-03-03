package gg.mylittleplanet.manager.ptero.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PowerRequest {
    @JsonProperty("signal")
    private String signal;

    public static PowerRequest start()   { return new PowerRequest("start");   }
    public static PowerRequest stop()    { return new PowerRequest("stop");    }
    public static PowerRequest restart() { return new PowerRequest("restart"); }
    public static PowerRequest kill()    { return new PowerRequest("kill");    }
}