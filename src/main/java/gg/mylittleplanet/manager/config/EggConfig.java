package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class EggConfig {
    private String file;           // path to egg JSON file
    private String dockerImage;    // override docker image, optional
}