package taskmanagement.dto.Response;


import taskmanagement.entity.Task;
import taskmanagement.enums.Priority;
import taskmanagement.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate deadline;


    private Long projectId;
    private String projectName;
    private Long assigneeId;
    private String assigneeName;


    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;


    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;


    private Boolean overdue;


    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedBy(task.getUpdatedBy())
                .updatedAt(task.getUpdatedAt())
                .deleted(task.getDeleted())
                .deletedAt(task.getDeletedAt())
                .deletedBy(task.getDeletedBy())
                .overdue(task.isOverdue())
                .build();
    }
}

