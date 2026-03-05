package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.config.ManagerConfig;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConfigDebugController {

    private final ManagerConfig config;

    @GetMapping("/debug/config")
    public @NotNull String getConfig() {
        if (config.getPanel() == null) {
            return "ERROR: panel config is null";
        }
        return String.format("""
                Panel Config:
                  baseUrl: %s
                  appApiKey: %s (starts with: %s)
                  clientApiKey: %s (starts with: %s)
                """,
                config.getPanel().getBaseUrl(),
                config.getPanel().getAppApiKey() != null ? "SET" : "NULL",
                truncateKey(config.getPanel().getAppApiKey()),
                config.getPanel().getClientApiKey() != null ? "SET" : "NULL",
                truncateKey(config.getPanel().getClientApiKey())
        );
    }

    /**
     * Safely truncate an API key for debugging purposes.
     * 
     * @param key the key to truncate (may be null)
     * @return first 10 characters followed by "...", or "N/A" if null/too short
     */
    private @NotNull String truncateKey(@Nullable String key) {
        return key != null && key.length() > 10 
            ? key.substring(0, 10) + "..." 
            : "N/A";
    }
}

