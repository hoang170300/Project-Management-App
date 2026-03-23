package taskmanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO("To Do", "Chưa bắt đầu"),
    IN_PROGRESS("In Progress", "Đang thực hiện"),
    DONE("Done", "Hoàn thành");

    private final String displayName;
    private final String vietnameseName;

    TaskStatus(String displayName, String vietnameseName) {
        this.displayName = displayName;
        this.vietnameseName = vietnameseName;
    }

    public String getDisplayName() { return displayName; }
    public String getVietnameseName() { return vietnameseName; }

    public boolean canTransitionTo(TaskStatus newStatus) {
        if (this == TODO && newStatus == DONE) return false;
        if (this == DONE) return false;
        return true;
    }

     @JsonValue
    public String getName() {
        return this.name();
    }

    @JsonCreator
    public static TaskStatus fromString(String value) {
        if (value == null) return null;
        for (TaskStatus status : TaskStatus.values()) {
            if (status.name().equalsIgnoreCase(value)
                    || status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TaskStatus: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
