package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("fqdn")
    private String fqdn;

    @JsonProperty("location_id")
    private int locationId;

    @JsonProperty("memory")
    private int memory;

    @JsonProperty("disk")
    private int disk;

    @JsonProperty("daemon_base")
    private String daemonBase;

    @JsonProperty("daemon_sftp")
    private int daemonSftp;

    @JsonProperty("daemon_listen")
    private int daemonListen;
}