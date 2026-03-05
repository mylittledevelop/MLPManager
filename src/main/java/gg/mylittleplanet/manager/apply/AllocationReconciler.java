package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.config.NodeDefinitionConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.AllocationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationReconciler {

    private final PteroAppClient appClient;
    private final ManagerConfig config;

    public void reconcile(int nodeId, @NotNull ApplyResult result) {
        final NodeDefinitionConfig.PortRange range = config.getNode().getPortRange();
        final List<AllocationDto> existing = appClient.listAllocations(nodeId);

        final Set<Integer> existingPorts = existing.stream()
                .map(AllocationDto::getPort)
                .collect(Collectors.toSet());

        final long missingCount = range.portStream()
                .filter(port -> !existingPorts.contains(port))
                .count();

        if (missingCount == 0) {
            result.skipped("All allocations already exist for range "
                    + range.getFrom() + "-" + range.getTo());
            return;
        }

        // Create all missing ports in one API call using range syntax
        appClient.createAllocations(
                nodeId,
                config.getNode().getFqdn(),
                List.of(range.getFrom() + "-" + range.getTo())
        );
        result.created("Created " + missingCount + " allocations for ports "
                + range.getFrom() + "-" + range.getTo());
    }
}