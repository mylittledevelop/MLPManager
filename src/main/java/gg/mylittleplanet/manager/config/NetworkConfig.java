package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkConfig {
    // Defines startup order by server type, top = first to start / last to stop
    @NotNull
    private List<String> startupOrder = new ArrayList<>();
}