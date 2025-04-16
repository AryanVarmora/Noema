package noema;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports temporal reasoning in Noema
 * Handles time-based conditions and temporal facts
 */
public class TimeBasedCondition {
    
    // Constants for time units
    public static final long SECONDS = 1;
    public static final long MINUTES = 60;
    public static final long HOURS = 3600;
    public static final long DAYS = 86400;
    
    // Storage for time-based facts
    private final Map<String, Map<String, LocalDateTime>> timeEvents = new HashMap<>();
    
    // Standard time format
    private static final DateTimeFormatter timeFormatter = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Record a time-based event
     * @param entity The entity (character, object) involved
     * @param event The type of event
     * @param time The time of the event (or now if not specified)
     */
    public void recordEvent(String entity, String event, LocalDateTime time) {
        if (!timeEvents.containsKey(entity)) {
            timeEvents.put(entity, new HashMap<>());
        }
        
        timeEvents.get(entity).put(event, time);
    }
    
    public void recordEvent(String entity, String event) {
        recordEvent(entity, event, LocalDateTime.now());
    }
    
    /**
     * Get the time of a specific event for an entity
     * @param entity The entity to check
     * @param event The event type
     * @return The time of the event, or null if not found
     */
    public LocalDateTime getEventTime(String entity, String event) {
        if (!timeEvents.containsKey(entity)) {
            return null;
        }
        
        return timeEvents.get(entity).get(event);
    }
    
    /**
     * Check if two events happened within a time window
     * @param entity1 First entity
     * @param event1 First event
     * @param entity2 Second entity
     * @param event2 Second event
     * @param duration Maximum duration between events
     * @return True if the events happened within the specified duration
     */
    public boolean eventsWithinDuration(
            String entity1, String event1, 
            String entity2, String event2, 
            Duration duration) {
        
        LocalDateTime time1 = getEventTime(entity1, event1);
        LocalDateTime time2 = getEventTime(entity2, event2);
        
        if (time1 == null || time2 == null) {
            return false;
        }
        
        Duration difference = Duration.between(time1, time2).abs();
        return difference.compareTo(duration) <= 0;
    }
    
    /**
     * Check if two events for the same entity happened within a time window
     * @param entity The entity to check
     * @param event1 First event
     * @param event2 Second event
     * @param duration Maximum duration between events
     * @return True if the events happened within the specified duration
     */
    public boolean eventsWithinDuration(
            String entity, String event1, String event2, Duration duration) {
        
        return eventsWithinDuration(entity, event1, entity, event2, duration);
    }
    
    /**
     * Parse a time duration from a string with units
     * @param durationStr String like "5 minutes" or "2 hours"
     * @return Duration object representing the time period
     */
    public static Duration parseDuration(String durationStr) {
        String[] parts = durationStr.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid duration format: " + durationStr);
        }
        
        long amount = Long.parseLong(parts[0]);
        String unit = parts[1].toLowerCase();
        
        switch (unit) {
            case "second":
            case "seconds":
                return Duration.ofSeconds(amount);
                
            case "minute":
            case "minutes":
                return Duration.ofMinutes(amount);
                
            case "hour":
            case "hours":
                return Duration.ofHours(amount);
                
            case "day":
            case "days":
                return Duration.ofDays(amount);
                
            default:
                throw new IllegalArgumentException("Unknown time unit: " + unit);
        }
    }
    
    /**
     * Parse a time string into a LocalDateTime
     * @param timeStr Time string in format "yyyy-MM-dd HH:mm:ss"
     * @return Parsed LocalDateTime
     */
    public static LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr, timeFormatter);
    }
    
    /**
     * Format a LocalDateTime as a string
     * @param time The time to format
     * @return Formatted time string
     */
    public static String formatTime(LocalDateTime time) {
        return time.format(timeFormatter);
    }
}