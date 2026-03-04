package taskmanagement.enums;


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

    public String getDisplayName() {
        return displayName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public int getLevel() {
        return level;
    }


    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
