package gg.mylittleplanet.manager.ptero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.mylittleplanet.manager.config.ManagerConfig;
import gg.mylittleplanet.manager.ptero.dto.PteroDataWrapper;
import gg.mylittleplanet.manager.ptero.dto.PteroListWrapper;
import gg.mylittleplanet.manager.ptero.dto.client.ClientServerDto;
import gg.mylittleplanet.manager.ptero.dto.client.PowerRequest;
import gg.mylittleplanet.manager.ptero.dto.client.SendCommandRequest;
import gg.mylittleplanet.manager.ptero.dto.client.ServerResourcesDto;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class PteroClientClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PteroClientClient(ManagerConfig config, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(config.getPanel().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getPanel().getClientApiKey())
                .defaultHeader("Accept", "application/vnd.pterodactyl.v1+json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ── Servers ────────────────────────────────────────────────────────────

    public @NotNull List<ClientServerDto> listServers() {
        final PteroListWrapper<ClientServerDto> response = get(
                "/api/client",
                new TypeReference<>() {}
        );
        return unwrapList(response);
    }

    public @Nullable ServerResourcesDto getResources(@NotNull String serverUuid) {
        final PteroDataWrapper<ServerResourcesDto> response = get(
                "/api/client/servers/" + serverUuid + "/resources",
                new TypeReference<>() {}
        );
        return response != null ? response.getAttributes() : null;
    }

    // ── Power ──────────────────────────────────────────────────────────────

    public void start(@NotNull String serverUuid) {
        log.info("Sending start signal to server {}", serverUuid);
        sendPower(serverUuid, PowerRequest.start());
    }

    public void stop(@NotNull String serverUuid) {
        log.info("Sending stop signal to server {}", serverUuid);
        sendPower(serverUuid, PowerRequest.stop());
    }

    public void restart(@NotNull String serverUuid) {
        log.info("Sending restart signal to server {}", serverUuid);
        sendPower(serverUuid, PowerRequest.restart());
    }

    public void kill(@NotNull String serverUuid) {
        log.warn("Sending kill signal to server {}", serverUuid);
        sendPower(serverUuid, PowerRequest.kill());
    }

    private void sendPower(@NotNull String serverUuid, @NotNull PowerRequest request) {
        post("/api/client/servers/" + serverUuid + "/power", request);
    }

    // ── Console ────────────────────────────────────────────────────────────

    public void sendCommand(@NotNull String serverUuid, @NotNull String command) {
        log.info("Sending command to {}: {}", serverUuid, command);
        post("/api/client/servers/" + serverUuid + "/command",
                new SendCommandRequest(command));
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────

    private <T> @Nullable T get(@NotNull String path, @NotNull TypeReference<T> type) {
        try {
            final String body = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) return null;
            return objectMapper.readValue(body, type);
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("GET " + path + " failed", e);
        }
    }

    private void post(@NotNull String path, @NotNull Object body) {
        try {
            restClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new PteroApiException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PteroApiException("POST " + path + " failed", e);
        }
    }

    private <T> @NotNull List<T> unwrapList(@Nullable PteroListWrapper<T> wrapper) {
        if (wrapper == null || wrapper.getData() == null) return List.of();
        return wrapper.getData().stream()
                .map(PteroDataWrapper::getAttributes)
                .toList();
    }
}