package gg.mylittleplanet.manager.apply;

import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.PteroAppClient;
import gg.mylittleplanet.manager.ptero.dto.app.LocationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationReconciler {

    private final PteroAppClient appClient;
    private final ManagerConfig config;

    public LocationDto reconcile(ApplyResult result) {
        final String locationName = config.getNode().getLocationName();
        final List<LocationDto> locations = appClient.listLocations();

        final Optional<LocationDto> existing = locations.stream()
                .filter(l -> l.getShortCode().equals(locationName))
                .findFirst();

        if (existing.isPresent()) {
            result.skipped("Location '" + locationName + "' already exists (id=" + existing.get().getId() + ")");
            return existing.get();
        }

        final LocationDto created = appClient.createLocation(locationName, locationName);
        result.created("Location '" + locationName + "' created (id=" + created.getId() + ")");
        return created;
    }
}