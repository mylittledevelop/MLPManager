package gg.mylittleplanet.manager.config;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class GitConfig {
    @Nullable
    private String pullUser;
    
    @Nullable
    private String pullToken;
    
    @Nullable
    private String scriptUser;
    
    @Nullable
    private String scriptUrl;
    
    @Nullable
    private String scriptBranch;
}