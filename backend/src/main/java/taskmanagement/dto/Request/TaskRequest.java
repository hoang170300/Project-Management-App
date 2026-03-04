package taskmanagement.dto.Request;


import taskmanagement.enums.Priority;
import taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "Tiêu đề task không được để trống")
    @Size(min = 3, max = 200, message = "Tiêu đề task phải từ 3-200 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String description;

    private TaskStatus status;

    @NotNull(message = "Priority không được null")
    private Priority priority;

    private LocalDate deadline;

    @NotNull(message = "ProjectId không được null")
    private Long projectId;

    private Long assigneeId;

    @NotNull(message = "CreatedBy không được null")
    private Long createdBy;

    private Long updatedBy;
}

