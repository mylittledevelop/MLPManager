package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class EggConfig {
    private int nestId;
    private int id;
    private String dockerImage;
}