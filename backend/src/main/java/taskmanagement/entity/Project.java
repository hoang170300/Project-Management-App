package taskmanagement.entity;

import taskmanagement.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Project {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;

    private LocalDate startDate;
    private LocalDate endDate;


    private Long createdById;
    private List<Long> memberIds = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Constructor =====
    public Project(Long id,
                   String name,
                   String description,
                   LocalDate startDate,
                   LocalDate endDate,
                   Long createdById) {

        validate(name, startDate, endDate, createdById);

        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ProjectStatus.PLANNING;
        this.createdById = createdById;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Validation =====
    private void validate(String name,
                          LocalDate startDate,
                          LocalDate endDate,
                          Long createdById) {

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Tên project không được để trống");

        if (startDate == null)
            throw new IllegalArgumentException("Ngày bắt đầu không được null");

        if (endDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");

        if (createdById == null)
            throw new IllegalArgumentException("Project phải có người tạo");
    }

    // ===== Business Logic =====

    public void addMember(Long userId) {
        if (status == ProjectStatus.CANCELLED)
            throw new IllegalStateException("Project đã bị hủy");

        if (!memberIds.contains(userId))
            memberIds.add(userId);

        this.updatedAt = LocalDateTime.now();
    }

    public void removeMember(Long userId) {
        memberIds.remove(userId);
        this.updatedAt = LocalDateTime.now();
    }

    public void addTask(Task task) {
        if (!status.canAddTask())
            throw new IllegalStateException("Project không thể thêm task ở trạng thái này");

        if (!task.getProjectId().equals(this.id))
            throw new IllegalArgumentException("Task không thuộc project này");

        tasks.add(task);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ProjectStatus newStatus) {
        if (this.status == ProjectStatus.COMPLETED ||
                this.status == ProjectStatus.CANCELLED)
            throw new IllegalStateException("Project đã kết thúc");

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getter =====

    public Long getId() { return id; }
    public ProjectStatus getStatus() { return status; }
    public List<Task> getTasks() { return tasks; }
    public List<Long> getMemberIds() { return memberIds; }

    // ===== equals & hashCode =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}