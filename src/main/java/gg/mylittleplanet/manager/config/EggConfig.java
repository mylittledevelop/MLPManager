package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class EggConfig {
    private int nestId;
    private int id;
    
    @Nullable
    private String dockerImage;
    
    private String serverJarfile = "server.jar";
    private String buildDir = "build";
    private String buildNumber = "latest";
    private String startScript = "start.sh";
}