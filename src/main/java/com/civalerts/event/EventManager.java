package com.civalerts.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("civ-alerts");
    private static final int MAX_ACTIVE = 10;
    private static final int MAX_HISTORY = 100;
    private static final long MAX_AGE_MS = 10L * 60L * 1000L; // 10 minutes

    private final List<CivEvent> active = new ArrayList<>();
    private final List<CivEvent> history = new ArrayList<>();

    public void addEvent(CivEventType type, String text, List<String> coordinates) {
        addEvent(new CivEvent(type, text, coordinates));
    }

    public synchronized void addEvent(CivEvent event) {
        active.add(event);
        history.add(event);

        if (active.size() > MAX_ACTIVE) {
            active.removeFirst();
        }
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }

        LOGGER.debug("Added event type={} active={} history={}", event.type(), active.size(), history.size());
    }

    public synchronized List<CivEvent> getActive(CivEventType type) {
        return active.stream()
                .filter(e -> e.type() == type)
                .collect(Collectors.toList());
    }

    public synchronized List<CivEvent> getActive() {
        return List.copyOf(active);
    }

    public synchronized List<CivEvent> getHistory() {
        return List.copyOf(history);
    }

    public synchronized void cleanOldEvents() {
        long cutoff = System.currentTimeMillis() - MAX_AGE_MS;
        int removed = 0;
        while (!active.isEmpty() && active.get(0).timestamp() < cutoff) {
            active.removeFirst();
            removed++;
        }
        if (removed > 0) {
            LOGGER.debug("Cleaned {} old active events", removed);
        }
    }

    public synchronized int count(CivEventType type) {
        return (int) active.stream().filter(e -> e.type() == type).count();
    }
}
