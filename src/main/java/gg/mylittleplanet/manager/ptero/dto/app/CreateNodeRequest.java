package gg.mylittleplanet.manager.ptero.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateNodeRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("location_id")
    private int locationId;

    @JsonProperty("fqdn")
    private String fqdn;

    @JsonProperty("scheme")
    private String scheme;          // "http" or "https"

    @JsonProperty("memory")
    private int memory;

    @JsonProperty("memory_overallocate")
    private int memoryOverallocate;

    @JsonProperty("disk")
    private int disk;

    @JsonProperty("disk_overallocate")
    private int diskOverallocate;

    @JsonProperty("upload_size")
    private int uploadSize;

    @JsonProperty("daemon_sftp")
    private int daemonSftp;

    @JsonProperty("daemon_listen")
    private int daemonListen;

    @JsonProperty("behind_proxy")
    private boolean behindProxy;

    @JsonProperty("maintenance_mode")
    private boolean maintenanceMode;
}