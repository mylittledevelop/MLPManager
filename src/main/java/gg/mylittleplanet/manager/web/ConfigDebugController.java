package gg.mylittleplanet.manager.web;

import gg.mylittleplanet.manager.config.ManagerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConfigDebugController {

    private final ManagerConfig config;

    @GetMapping("/debug/config")
    public String getConfig() {
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
                config.getPanel().getAppApiKey() != null && config.getPanel().getAppApiKey().length() > 10 
                    ? config.getPanel().getAppApiKey().substring(0, 10) : "N/A",
                config.getPanel().getClientApiKey() != null ? "SET" : "NULL",
                config.getPanel().getClientApiKey() != null && config.getPanel().getClientApiKey().length() > 10 
                    ? config.getPanel().getClientApiKey().substring(0, 10) : "N/A"
        );
    }
}

