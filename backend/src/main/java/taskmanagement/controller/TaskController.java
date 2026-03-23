package taskmanagement.controller;

import taskmanagement.dto.Response.ApiResponse;
import taskmanagement.dto.Request.TaskRequest;
import taskmanagement.dto.Response.TaskResponse;
import taskmanagement.enums.TaskStatus;
import taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "3. Tasks", description = "Quản lý Task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(
            summary = "Tạo task mới",
            description = "Project phải ACTIVE. createdBy = ID người tạo."
    )
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping
    @Operation(summary = "Danh sách tất cả task (active)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success(taskService.getAllTasks()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết task theo ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully",
                taskService.updateTask(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft delete task",
            description = "?deletedBy=1 | Không được xóa task DONE"
    )
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestParam Long deletedBy) {
        taskService.deleteTask(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Khôi phục task đã xóa")
    public ResponseEntity<ApiResponse<TaskResponse>> restoreTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task restored successfully",
                taskService.restoreTask(id)));
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Cập nhật status task",
            description = "Body: {\"status\": \"IN_PROGRESS\", \"updatedBy\": 1}\n\nFlow: TODO → IN_PROGRESS → DONE (không skip)"
    )
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        TaskStatus newStatus = TaskStatus.valueOf((String) body.get("status"));
        Long updatedBy = Long.valueOf(body.get("updatedBy").toString());
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully",
                taskService.updateTaskStatus(id, newStatus, updatedBy)));
    }

    @PutMapping("/{id}/assign/{userId}")
    @Operation(
            summary = "Gán task cho user",
            description = "?updatedBy=1 | User phải còn active (chưa bị soft delete)"
    )
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam Long updatedBy) {
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully",
                taskService.assignTask(id, userId, updatedBy)));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Danh sách task theo project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByProject(projectId)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Danh sách task theo user (assignee)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByUser(userId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Danh sách task theo status: TODO | IN_PROGRESS | DONE")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByStatus(
            @Parameter(description = "TaskStatus") @PathVariable TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByStatus(status)));
    }
}