package taskmanagement.enums;

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

    public String getDisplayName() {
        return displayName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }


    public boolean canTransitionTo(TaskStatus newStatus) {
        if (this == TODO && newStatus == DONE) {
            return false;
        }
        if (this == DONE) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
