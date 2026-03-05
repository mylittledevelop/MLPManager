package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.ptero.dto.app.LocationDto;
import gg.mylittleplanet.manager.ptero.dto.app.NodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final LocationReconciler locationReconciler;
    private final NodeReconciler nodeReconciler;
    private final AllocationReconciler allocationReconciler;
    private final ServerReconciler serverReconciler;

    public @NotNull ApplyResult apply() {
        final ApplyResult result = new ApplyResult();

        try {
            result.info("Starting apply...");

            // Step 1 — Location
            result.info("── Reconciling location...");
            final LocationDto location = locationReconciler.reconcile(result);

            // Step 2 — Node
            result.info("── Reconciling node...");
            final NodeDto node = nodeReconciler.reconcile(location, result);

            // Step 3 — Allocations
            result.info("── Reconciling allocations...");
            allocationReconciler.reconcile(node.getId(), result);

            // Step 4 — Servers
            result.info("── Reconciling servers...");
            serverReconciler.reconcile(node.getId(), result);

            result.info("Apply complete.");

        } catch (Exception e) {
            result.error("Apply failed: " + e.getMessage());
            log.error("Apply failed", e);
        }

        return result;
    }
}