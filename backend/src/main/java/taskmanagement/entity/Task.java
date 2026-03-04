package taskmanagement.entity;

import taskmanagement.enums.Priority;
import taskmanagement.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate deadline;

    private Long projectId;
    private Long assigneeId;
    private Long createdById;

    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Constructor =====

    public Task(String title,
                String description,
                Long projectId,
                Long assigneeId) {

        validate(title, projectId, deadline);

        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TaskStatus.TODO;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.deadline = deadline;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.createdById = createdById;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public Task(Long id, String title, String description, Long projectId, Long createdById) {
        this(title, description, projectId, null);
    }
    private void validate(String title, Long projectId, LocalDate deadline) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title không được để trống");

        if (title.length() > 200)
            throw new IllegalArgumentException("Title không được vượt quá 200 ký tự");

        if (projectId == null)
            throw new IllegalArgumentException("Task phải thuộc về một Project");

        if (deadline != null && deadline.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Deadline không thể ở quá khứ");
    }

    // ===== Business Logic =====

    public void updateStatus(TaskStatus newStatus) {
        if (newStatus == null)
            throw new IllegalArgumentException("Status mới không được null");

        if (!this.status.canTransitionTo(newStatus))
            throw new IllegalStateException("Không thể chuyển từ " + this.status + " sang " + newStatus);

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo(Long userId) {
        if (userId == null)
            throw new IllegalArgumentException("User ID không được null");

        if (this.status == TaskStatus.DONE)
            throw new IllegalStateException("Task đã DONE, không thể assign lại");

        this.assigneeId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTask(String newTitle, String newDescription, Priority newPriority, LocalDate newDeadline) {
        if (this.status == TaskStatus.DONE)
            throw new IllegalStateException("Task đã DONE, không thể chỉnh sửa");

        if (newTitle != null && !newTitle.trim().isEmpty())
            this.title = newTitle;

        if (newDescription != null)
            this.description = newDescription;

        if (newPriority != null)
            this.priority = newPriority;

        if (newDeadline != null) {
            if (newDeadline.isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Deadline không thể ở quá khứ");
            this.deadline = newDeadline;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.status == TaskStatus.IN_PROGRESS)
            throw new IllegalStateException("Không thể xóa task đang IN_PROGRESS");

        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return deadline != null && deadline.isBefore(LocalDate.now()) && status != TaskStatus.DONE;
    }

    public boolean isAssigned() {
        return assigneeId != null;
    }

    // ===== Getter =====

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public LocalDate getDeadline() { return deadline; }
    public Long getProjectId() { return projectId; }
    public Long getAssigneeId() { return assigneeId; }
    public boolean isDeleted() { return deleted; }

    // ===== equals & hashCode =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status=" + status + ", priority=" + priority + "}";
    }
}
