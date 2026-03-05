package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class PanelConfig {
    @Nullable
    private String baseUrl;
    
    @Nullable
    private String appApiKey;
    
    @Nullable
    private String clientApiKey;
}