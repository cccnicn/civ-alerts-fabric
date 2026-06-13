package com.civalerts.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("civ-alerts");
    private static final int MAX_ACTIVE = 10;
    private static final int MAX_HISTORY = 100;

    private final List<CivEvent> active = new ArrayList<>();
    private final List<CivEvent> history = new ArrayList<>();

    public EventManager() {}

    public synchronized void addEvent(CivEventType type, String text, List<String> coordinates) {
        CivEvent event = new CivEvent(type, text, coordinates);
        active.add(event);
        history.add(event);

        if (active.size() > MAX_ACTIVE) {
            active.removeFirst();
        }
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }

        LOGGER.debug("Added event type={} activeSize={} historySize={}", type, active.size(), history.size());
    }

    public synchronized List<CivEvent> getActive() {
        return List.copyOf(active);
    }

    public synchronized List<CivEvent> getHistory() {
        return List.copyOf(history);
    }
}
