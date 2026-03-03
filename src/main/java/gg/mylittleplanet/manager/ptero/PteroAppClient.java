package gg.mylittleplanet.manager.ptero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.dto.PteroDataWrapper;
import gg.mylittleplanet.manager.ptero.dto.PteroListWrapper;
import gg.mylittleplanet.manager.ptero.dto.app.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class PteroAppClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PteroAppClient(ManagerConfig config, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(config.getPanel().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getPanel().getAppApiKey())
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ── Node Configuration (Wings config) ─────────────────────────────────

    public String getNodeConfiguration(int nodeId) {
        // Returns raw YAML — not JSON, so we just return the string directly
        try {
            return restClient.get()
                    .uri("/api/application/nodes/" + nodeId + "/configuration")
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("GET node configuration failed", e);
        }
    }

// ── Eggs ───────────────────────────────────────────────────────────────

    public EggDto getEgg(int nestId, int eggId) {
        final PteroDataWrapper<EggDto> response = get(
                "/api/application/nests/" + nestId + "/eggs/" + eggId,
                new TypeReference<>() {}
        );
        return response.getAttributes();
    }

// ── Users ──────────────────────────────────────────────────────────────

    public List<UserDto> listUsers() {
        final PteroListWrapper<UserDto> response = get(
                "/api/application/users",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }
    
    
    // ── Locations ──────────────────────────────────────────────────────────

    public List<LocationDto> listLocations() {
        final PteroListWrapper<LocationDto> response = get(
                "/api/application/locations",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public LocationDto createLocation(String shortCode, String description) {
        log.info("Creating location '{}'", shortCode);
        final PteroDataWrapper<LocationDto> response = post(
                "/api/application/locations",
                new CreateLocationRequest(shortCode, description),
                new TypeReference<>() {}
        );
        return response.getAttributes();
    }

    // ── Nodes ──────────────────────────────────────────────────────────────

    public List<NodeDto> listNodes() {
        final PteroListWrapper<NodeDto> response = get(
                "/api/application/nodes",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public NodeDto createNode(CreateNodeRequest request) {
        log.info("Creating node '{}'", request.getName());
        final PteroDataWrapper<NodeDto> response = post(
                "/api/application/nodes",
                request,
                new TypeReference<>() {}
        );
        return response.getAttributes();
    }

    // ── Allocations ────────────────────────────────────────────────────────

    public List<AllocationDto> listAllocations(int nodeId) {
        final PteroListWrapper<AllocationDto> response = get(
                "/api/application/nodes/" + nodeId + "/allocations",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public void createAllocations(int nodeId, String ip, List<String> ports) {
        log.info("Creating allocations on node {} for ports {}", nodeId, ports);
        post(
                "/api/application/nodes/" + nodeId + "/allocations",
                new CreateAllocationRequest(ip, ports),
                new TypeReference<PteroDataWrapper<Void>>() {}
        );
    }

    // ── Servers ────────────────────────────────────────────────────────────

    public List<ServerDto> listServers() {
        final PteroListWrapper<ServerDto> response = get(
                "/api/application/servers",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public ServerDto createServer(CreateServerRequest request) {
        log.info("Creating server '{}'", request.getName());
        final PteroDataWrapper<ServerDto> response = post(
                "/api/application/servers",
                request,
                new TypeReference<>() {}
        );
        return response.getAttributes();
    }

    public ServerDto updateStartup(int serverId, UpdateStartupRequest request) {
        log.info("Updating startup config for server {}", serverId);
        final PteroDataWrapper<ServerDto> response = patch(
                "/api/application/servers/" + serverId + "/startup",
                request,
                new TypeReference<>() {}
        );
        return response.getAttributes();
    }

    public void deleteServer(int serverId) {
        log.warn("Deleting server {}", serverId);
        delete("/api/application/servers/" + serverId);
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────

    private <T> T get(String path, TypeReference<T> type) {
        try {
            final String body = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(body, type);
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("GET " + path + " failed", e);
        }
    }

    private <T> T post(String path, Object body, TypeReference<T> type) {
        try {
            final String response = restClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            if (response == null || response.isBlank()) return null;
            return objectMapper.readValue(response, type);
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("POST " + path + " failed", e);
        }
    }

    private <T> T patch(String path, Object body, TypeReference<T> type) {
        try {
            final String response = restClient.patch()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            if (response == null || response.isBlank()) return null;
            return objectMapper.readValue(response, type);
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("PATCH " + path + " failed", e);
        }
    }

    private void delete(String path) {
        try {
            restClient.delete()
                    .uri(path)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
            }
            // 404 on delete is fine — already gone
        } catch (Exception e) {
            throw new PteroApiException("DELETE " + path + " failed", e);
        }
    }

    private <T> List<T> unwrapList(PteroListWrapper<T> wrapper) {
        if (wrapper == null || wrapper.getData() == null) return List.of();
        return wrapper.getData().stream()
                .map(PteroDataWrapper::getAttributes)
                .toList();
    }
}