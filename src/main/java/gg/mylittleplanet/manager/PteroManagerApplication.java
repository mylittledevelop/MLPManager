package gg.mylittleplanet.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class PteroManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PteroManagerApplication.class, args);
    }
}