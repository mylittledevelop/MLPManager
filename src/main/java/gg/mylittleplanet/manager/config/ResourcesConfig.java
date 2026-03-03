package gg.mylittleplanet.manager.config;

import lombok.Data;

@Data
public class ResourcesConfig {
    private int memoryMb;
    private int diskMb;
    private int cpu;
}