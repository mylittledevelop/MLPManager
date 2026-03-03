package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.config.NodeDefinitionConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.CreateNodeRequest;
import gg.mylittleplanet.manager.ptero.dto.app.LocationDto;
import gg.mylittleplanet.manager.ptero.dto.app.NodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeReconciler {

    private final PteroAppClient appClient;
    private final ManagerConfig config;
    private final WingsConfigWriter wingsConfigWriter;

    public NodeDto reconcile(LocationDto location, ApplyResult result) {
        final NodeDefinitionConfig nodeCfg = config.getNode();
        final List<NodeDto> nodes = appClient.listNodes();

        final Optional<NodeDto> existing = nodes.stream()
                .filter(n -> n.getName().equals(nodeCfg.getName()))
                .findFirst();

        if (existing.isPresent()) {
            result.skipped("Node '" + nodeCfg.getName() + "' already exists (id=" + existing.get().getId() + ")");
            return existing.get();
        }

        final NodeDto created = appClient.createNode(CreateNodeRequest.builder()
                .name(nodeCfg.getName())
                .locationId(location.getId())
                .fqdn(nodeCfg.getFqdn())
                .scheme("http")
                .memory(nodeCfg.getResources().getMemoryMb())
                .memoryOverallocate(0)
                .disk(nodeCfg.getResources().getDiskMb())
                .diskOverallocate(0)
                .uploadSize(100)
                .daemonSftp(2022)
                .daemonListen(8080)
                .behindProxy(false)
                .maintenanceMode(false)
                .build());

        result.created("Node '" + nodeCfg.getName() + "' created (id=" + created.getId() + ")");

        // Write Wings config and restart Wings so it connects to the panel
        wingsConfigWriter.writeAndRestart(created.getId(), result);

        return created;
    }
}