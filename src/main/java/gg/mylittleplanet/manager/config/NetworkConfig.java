package gg.mylittleplanet.manager.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkConfig {
    // Defines startup order by server type, top = first to start / last to stop
    private List<String> startupOrder = new ArrayList<>();
}