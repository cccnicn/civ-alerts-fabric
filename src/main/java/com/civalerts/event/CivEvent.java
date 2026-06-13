package com.civalerts.event;

import java.util.Collections;
import java.util.List;

public final class CivEvent {
    private final CivEventType type;
    private final long timestamp;
    private final String text;
    private final List<String> coordinates;
    private final int entryIndex;

    public CivEvent(CivEventType type, String text, List<String> coordinates, int entryIndex) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.text = text;
        this.coordinates = coordinates != null ? List.copyOf(coordinates) : Collections.emptyList();
        this.entryIndex = entryIndex;
    }

    public CivEvent(CivEventType type, String text, List<String> coordinates) {
        this(type, text, coordinates, 0);
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

    public int entryIndex() {
        return entryIndex;
    }

    public long ageSeconds() {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}
