package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.config.NodeDefinitionConfig;
import gg.mylittleplanet.manager.config.ResourcesConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.CreateNodeRequest;
import gg.mylittleplanet.manager.ptero.dto.app.LocationDto;
import gg.mylittleplanet.manager.ptero.dto.app.NodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeReconciler {

    private final PteroAppClient appClient;
    private final ManagerConfig config;
    private final WingsConfigWriter wingsConfigWriter;

    public @NotNull NodeDto reconcile(@NotNull LocationDto location, @NotNull ApplyResult result) {
        final NodeDefinitionConfig nodeCfg = Objects.requireNonNull(config.getNode(), "manager.node must be configured");
        final String nodeName = Objects.requireNonNull(nodeCfg.getName(), "manager.node.name must be configured");
        final String fqdn = Objects.requireNonNull(nodeCfg.getFqdn(), "manager.node.fqdn must be configured");
        final ResourcesConfig resources = Objects.requireNonNull(nodeCfg.getResources(), "manager.node.resources must be configured");
        final List<NodeDto> nodes = appClient.listNodes();

        final Optional<NodeDto> existing = nodes.stream()
                .filter(n -> nodeName.equals(n.getName()))
                .findFirst();

        if (existing.isPresent()) {
            final NodeDto node = existing.get();
            result.skipped("Node '" + nodeName + "' already exists (id=" + node.getId() + ")");
            // Re-apply Wings config in case previous restart failed or config changed.
            wingsConfigWriter.writeAndRestart(node.getId(), result);
            return node;
        }

        final NodeDto created = appClient.createNode(CreateNodeRequest.builder()
                .name(nodeName)
                .locationId(location.getId())
                .fqdn(fqdn)
                .scheme("http")
                .memory(resources.getMemoryMb())
                .memoryOverallocate(0)
                .disk(resources.getDiskMb())
                .diskOverallocate(0)
                .uploadSize(100)
                .daemonSftp(2022)
                .daemonListen(8080)
                .behindProxy(false)
                .maintenanceMode(false)
                .build());

        result.created("Node '" + nodeName + "' created (id=" + created.getId() + ")");

        // Write Wings config and restart Wings so it connects to the panel.
        wingsConfigWriter.writeAndRestart(created.getId(), result);

        return created;
    }
}