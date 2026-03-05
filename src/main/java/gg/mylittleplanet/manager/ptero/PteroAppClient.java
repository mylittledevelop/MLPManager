package gg.mylittleplanet.manager.ptero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.dto.PteroDataWrapper;
import gg.mylittleplanet.manager.ptero.dto.PteroListWrapper;
import gg.mylittleplanet.manager.ptero.dto.app.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        final String baseUrl = config.getPanel().getBaseUrl();
        final String apiKey = config.getPanel().getAppApiKey();
        
        log.info("Initializing PteroAppClient with baseUrl: {}", baseUrl);
        log.info("API Key configured: {}", apiKey != null && !apiKey.isBlank());
        log.debug("API Key prefix: {}", apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null");
        
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/vnd.pterodactyl.v1+json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ── Node Configuration (Wings config) ─────────────────────────────────

    public @NotNull String getNodeConfiguration(int nodeId) {
        // Returns raw YAML — not JSON, so we just return the string directly
        try {
            final String body = restClient.get()
                    .uri("/api/application/nodes/" + nodeId + "/configuration")
                    .retrieve()
                    .body(String.class);
            if (body == null) {
                throw new PteroApiException("Node configuration returned empty response", null);
            }
            return body;
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("GET node configuration failed", e);
        }
    }

// ── Eggs ───────────────────────────────────────────────────────────────

    public @Nullable EggDto getEgg(int nestId, int eggId) {
        final PteroDataWrapper<EggDto> response = get(
                "/api/application/nests/" + nestId + "/eggs/" + eggId + "?include=variables",
                new TypeReference<>() {}
        );
        return response != null ? response.getAttributes() : null;
    }

// ── Users ──────────────────────────────────────────────────────────────

    public @NotNull List<UserDto> listUsers() {
        final PteroListWrapper<UserDto> response = get(
                "/api/application/users",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }
    
    
    // ── Locations ──────────────────────────────────────────────────────────

    public @NotNull List<LocationDto> listLocations() {
        final PteroListWrapper<LocationDto> response = get(
                "/api/application/locations",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public @NotNull LocationDto createLocation(@NotNull String shortCode, @NotNull String description) {
        log.info("Creating location '{}'", shortCode);
        final PteroDataWrapper<LocationDto> response = post(
                "/api/application/locations",
                new CreateLocationRequest(shortCode, description),
                new TypeReference<>() {}
        );
        if (response == null) {
            throw new PteroApiException("Location creation returned null response", null);
        }
        return response.getAttributes();
    }

    // ── Nodes ──────────────────────────────────────────────────────────────

    public @NotNull List<NodeDto> listNodes() {
        final PteroListWrapper<NodeDto> response = get(
                "/api/application/nodes",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public @NotNull NodeDto createNode(@NotNull CreateNodeRequest request) {
        log.info("Creating node '{}'", request.getName());
        final PteroDataWrapper<NodeDto> response = post(
                "/api/application/nodes",
                request,
                new TypeReference<>() {}
        );
        if (response == null) {
            throw new PteroApiException("Node creation returned null response", null);
        }
        return response.getAttributes();
    }

    // ── Allocations ────────────────────────────────────────────────────────

    public @NotNull List<AllocationDto> listAllocations(int nodeId) {
        final PteroListWrapper<AllocationDto> response = get(
                "/api/application/nodes/" + nodeId + "/allocations",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public void createAllocations(int nodeId, @NotNull String ip, @NotNull List<String> ports) {
        log.info("Creating allocations on node {} for ports {}", nodeId, ports);
        post(
                "/api/application/nodes/" + nodeId + "/allocations",
                new CreateAllocationRequest(ip, ports),
                new TypeReference<PteroDataWrapper<Void>>() {}
        );
    }

    // ── Servers ────────────────────────────────────────────────────────────

    public @NotNull List<ServerDto> listServers() {
        final PteroListWrapper<ServerDto> response = get(
                "/api/application/servers",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public @NotNull ServerDto createServer(@NotNull CreateServerRequest request) {
        log.info("Creating server '{}'", request.getName());
        final PteroDataWrapper<ServerDto> response = post(
                "/api/application/servers",
                request,
                new TypeReference<>() {}
        );
        if (response == null) {
            throw new PteroApiException("Server creation returned null response", null);
        }
        return response.getAttributes();
    }

    public @NotNull ServerDto updateStartup(int serverId, @NotNull UpdateStartupRequest request) {
        log.info("Updating startup config for server {}", serverId);
        final PteroDataWrapper<ServerDto> response = patch(
                "/api/application/servers/" + serverId + "/startup",
                request,
                new TypeReference<>() {}
        );
        if (response == null) {
            throw new PteroApiException("Startup update returned null response", null);
        }
        return response.getAttributes();
    }

    public void deleteServer(int serverId) {
        log.warn("Deleting server {}", serverId);
        delete("/api/application/servers/" + serverId);
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────

    private <T> @Nullable T parseJsonResponse(@Nullable String body, TypeReference<T> type) throws Exception {
        if (body == null || body.isBlank()) return null;
        return objectMapper.readValue(body, type);
    }

    private <T> @Nullable T get(String path, TypeReference<T> type) {
        try {
            log.debug("GET request to: {}", path);
            final String body = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);
            return parseJsonResponse(body, type);
        } catch (HttpClientErrorException e) {
            log.error("GET {} failed with status {}: {}", path, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("GET {} failed", path, e);
            throw new PteroApiException("GET " + path + " failed", e);
        }
    }

    private <T> @Nullable T post(String path, Object body, TypeReference<T> type) {
        try {
            log.debug("POST request to: {}", path);
            String jsonBody = null;
            try {
                jsonBody = objectMapper.writeValueAsString(body);
                log.debug("Request body: {}", jsonBody);
            } catch (Exception e) {
                log.debug("Could not serialize request body for logging", e);
            }
            final String response = restClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseJsonResponse(response, type);
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("POST {} failed with status {}: {}", path, e.getStatusCode(), errorBody);
            throw new PteroApiException(e.getStatusCode().value(), errorBody);
        } catch (Exception e) {
            log.error("POST {} failed", path, e);
            throw new PteroApiException("POST " + path + " failed", e);
        }
    }

    private <T> @Nullable T patch(String path, Object body, TypeReference<T> type) {
        try {
            final String response = restClient.patch()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseJsonResponse(response, type);
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

    private <T> @NotNull List<T> unwrapList(@Nullable PteroListWrapper<T> wrapper) {
        if (wrapper == null || wrapper.getData() == null) return List.of();
        return wrapper.getData().stream()
                .map(PteroDataWrapper::getAttributes)
                .toList();
    }
}