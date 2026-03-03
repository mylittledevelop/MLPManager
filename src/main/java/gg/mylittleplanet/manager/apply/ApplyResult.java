package gg.mylittleplanet.manager.apply;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplyResult {

    public enum Status { CREATED, UPDATED, SKIPPED, INFO, ERROR }

    @Data
    public static class LogEntry {
        private final Status status;
        private final String message;
    }

    private final List<LogEntry> log = new ArrayList<>();
    private boolean success = true;

    public void created(String message) {
        log.add(new LogEntry(Status.CREATED, message));
    }

    public void updated(String message) {
        log.add(new LogEntry(Status.UPDATED, message));
    }

    public void skipped(String message) {
        log.add(new LogEntry(Status.SKIPPED, message));
    }

    public void info(String message) {
        log.add(new LogEntry(Status.INFO, message));
    }

    public void error(String message) {
        log.add(new LogEntry(Status.ERROR, message));
        success = false;
    }
}