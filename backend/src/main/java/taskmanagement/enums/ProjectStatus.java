package taskmanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    PLANNING("Planning", "Đang lên kế hoạch"),
    ACTIVE("Active", "Đang hoạt động"),
    COMPLETED("Completed", "Hoàn thành"),
    CANCELLED("Cancelled", "Đã hủy");

    private final String displayName;
    private final String vietnameseName;

    ProjectStatus(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }

    public String getDisplayName() { return displayName; }
    public String getVietnameseName() { return vietnameseName; }

    public boolean canAddTask() {
        return this == PLANNING || this == ACTIVE;
    }

    @JsonValue
    public String getName() {
        return this.name();  }

    @JsonCreator
    public static ProjectStatus fromString(String value) {
        if (value == null) return null;
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.name().equalsIgnoreCase(value)
                    || status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectStatus: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}