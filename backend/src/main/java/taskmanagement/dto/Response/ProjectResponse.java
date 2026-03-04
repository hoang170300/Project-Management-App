package taskmanagement.dto.Response;


import taskmanagement.entity.Project;
import taskmanagement.enums.ProjectStatus;
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
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    private Integer totalTasks;
    private Integer completedTasks;


    public static ProjectResponse fromEntity(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdBy(project.getCreatedBy())
                .createdAt(project.getCreatedAt())
                .updatedBy(project.getUpdatedBy())
                .updatedAt(project.getUpdatedAt())
                .deleted(project.getDeleted())
                .deletedAt(project.getDeletedAt())
                .deletedBy(project.getDeletedBy())
                .build();
    }

    public static ProjectResponse fromEntityWithStats(Project project, int totalTasks, int completedTasks) {
        ProjectResponse dto = fromEntity(project);
        if (dto != null) {
            dto.setTotalTasks(totalTasks);
            dto.setCompletedTasks(completedTasks);
        }
        return dto;
    }
}

