package taskmanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    LOW("Low", "Thấp", 1),
    MEDIUM("Medium", "Trung bình", 2),
    HIGH("High", "Cao", 3),
    URGENT("Urgent", "Khẩn cấp", 4);

    private final String displayName;
    private final String vietnameseName;
    private final int level;

    Priority(String displayName, String vietnameseName, int level) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
        this.level = level;
    }

    public String getDisplayName() { return displayName; }
    public String getVietnameseName() { return vietnameseName; }
    public int getLevel() { return level; }

    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }

    @JsonValue
    public String getName() {
        return this.name();
    }

    @JsonCreator
    public static Priority fromString(String value) {
        if (value == null) return null;
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value)
                    || priority.displayName.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown Priority: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}