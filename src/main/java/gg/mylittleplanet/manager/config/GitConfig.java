package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class GitConfig {
    private String pullUser;
    private String pullToken;
    private String scriptUser;
    private String scriptUrl;
    private String scriptBranch;
}