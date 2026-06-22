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
        if (text == null) {
            return null;
        }
        String plain = text.getString();
        if (plain == null || plain.isBlank()) {
            return null;
        }
        Matcher m = REPORT_PATTERN.matcher(plain);
        if (!m.find()) {
            return null;
        }

        CivEventType type = switch (m.group(1)) {
            case "Scout Report" -> CivEventType.SCOUT;
            case "Border Entrance Report" -> CivEventType.BORDER_ENTRANCE;
            case "Border Exit Report" -> CivEventType.BORDER_EXIT;
            default -> CivEventType.SCOUT;
        };

        int count = Integer.parseInt(m.group(2));

        String rawHover = extractHoverTextRecursive(text);
        List<String> entries = splitEntries(rawHover);

        LOGGER.info("parse: matched [Civ] type={} count={} rawHover='{}' entries={}",
                type, count, rawHover, entries.size());

        if (!entries.isEmpty() && entries.size() != count) {
            LOGGER.debug("Parsed entry count ({}) differs from reported count ({})", entries.size(), count);
        }

        return new ParsedReport(type, count, rawHover, entries);
    }

    private static String extractHoverTextRecursive(Text text) {
        StringBuilder sb = new StringBuilder();
        collectHoverText(text, sb, 0);
        return sb.toString().trim();
    }

    private static void collectHoverText(Text text, StringBuilder sb, int depth) {
        if (text == null) {
            return;
        }
        var style = text.getStyle();
        if (style != null) {
            HoverEvent event = style.getHoverEvent();
            if (event != null) {
                LOGGER.info("collectHoverText: depth={} found HoverEvent type={} class={}",
                        depth, event.getAction(), event.getClass().getSimpleName());
                if (event instanceof HoverEvent.ShowText showText) {
                    Text hoverValue = showText.value();
                    if (hoverValue != null) {
                        String str = hoverValue.getString();
                        LOGGER.info("collectHoverText: ShowText value='{}'", str);
                        if (str != null && !str.isBlank()) {
                            if (!sb.isEmpty()) { sb.append('\n'); }
                            sb.append(str);
                        }
                    }
                } else {
                    LOGGER.info("collectHoverText: NOT ShowText, skipping (action={})", event.getAction());
                }
            }
        }

        for (Text sibling : text.getSiblings()) {
            collectHoverText(sibling, sb, depth + 1);
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
