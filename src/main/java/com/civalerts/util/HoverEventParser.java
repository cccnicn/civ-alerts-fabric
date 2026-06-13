package com.civalerts.util;

import com.civalerts.event.CivEventType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HoverEventParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("civ-alerts");

    private static final Pattern REPORT_PATTERN = Pattern.compile(
            "\\[Civ\\]\\s*(Scout Report|Border Entrance Report|Border Exit Report)\\s*\\((\\d+)\\)"
    );

    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "(-?\\d{1,7})[\\s,;]+(-?\\d{1,7})[\\s,;]+(-?\\d{1,7})"
    );

    public record ParsedReport(CivEventType type, int reportedCount, String rawText, List<String> entries) {}

    private HoverEventParser() {}

    public static ParsedReport parse(Text text) {
        String plain = text.getString();
        Matcher m = REPORT_PATTERN.matcher(plain);
        if (!m.find()) {
            return null;
        }

        CivEventType type = switch (m.group(1)) {
            case "Scout Report" -> CivEventType.SCOUT;
            case "Border Entrance Report" -> CivEventType.BORDER_ENTRANCE;
            case "Border Exit Report" -> CivEventType.BORDER_EXIT;
            default -> CivEventType.SCOUT; // unreachable
        };

        int count = Integer.parseInt(m.group(2));

        String rawHover = extractHoverTextRecursive(text);
        List<String> entries = splitEntries(rawHover);

        if (!entries.isEmpty() && entries.size() != count) {
            LOGGER.debug("Parsed entry count ({}) differs from reported count ({})", entries.size(), count);
        }

        return new ParsedReport(type, count, rawHover, entries);
    }

    private static String extractHoverTextRecursive(Text text) {
        StringBuilder sb = new StringBuilder();
        collectHoverText(text, sb);
        return sb.toString().trim();
    }

    private static void collectHoverText(Text text, StringBuilder sb) {
        HoverEvent event = text.getStyle().getHoverEvent();
        if (event != null) {
            Text hoverValue = event.getValueFor(HoverEvent.Action.SHOW_TEXT);
            if (hoverValue != null) {
                String str = hoverValue.getString();
                if (!str.isBlank()) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(str);
                }
            }
        }

        for (Text sibling : text.getSiblings()) {
            collectHoverText(sibling, sb);
        }
    }

    private static List<String> splitEntries(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String line : raw.split("\\R")) {
            line = line.trim();
            if (!line.isEmpty()) {
                result.add(line);
            }
        }
        return result;
    }

    public static List<String> extractCoordinates(String text) {
        List<String> coords = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return coords;
        }
        Matcher m = COORDINATE_PATTERN.matcher(text);
        while (m.find()) {
            coords.add(m.group(1) + ", " + m.group(2) + ", " + m.group(3));
        }
        return coords;
    }
}
