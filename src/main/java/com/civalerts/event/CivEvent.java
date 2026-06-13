package com.civalerts.event;

import java.util.Collections;
import java.util.List;

public final class CivEvent {
    private final CivEventType type;
    private final long timestamp;
    private final String text;
    private final List<String> coordinates;

    public CivEvent(CivEventType type, String text, List<String> coordinates) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.text = text;
        this.coordinates = coordinates != null ? List.copyOf(coordinates) : Collections.emptyList();
    }

    public CivEventType type() {
        return type;
    }

    public long timestamp() {
        return timestamp;
    }

    public String text() {
        return text;
    }

    public List<String> coordinates() {
        return coordinates;
    }

    public long ageSeconds() {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}
