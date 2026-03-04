package taskmanagement.dto.Request;


import taskmanagement.enums.ProjectStatus;
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
public class ProjectRequest {

    @NotBlank(message = "Tên project không được để trống")
    @Size(min = 3, max = 100, message = "Tên project phải từ 3-100 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;

    @NotNull(message = "Trạng thái không được null")
    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "CreatedBy không được null")
    private Long createdBy;

    private Long updatedBy;
}

