package taskmanagement.enums;


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

    public String getDisplayName() {
        return displayName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }


    public boolean canAddTask() {
        return this == PLANNING || this == ACTIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
