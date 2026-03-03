package gg.mylittleplanet.manager.ptero;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class PteroApiException extends RuntimeException {
    private final int statusCode;

    public PteroApiException(int statusCode, String message) {
        super("Pterodactyl API error [" + statusCode + "]: " + message);
        this.statusCode = statusCode;
    }

    public PteroApiException(String message, @Nullable Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }
}