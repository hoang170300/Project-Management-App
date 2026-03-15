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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    /**
     * CREATE TASK
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request) {

        TaskResponse task = taskService.createTask(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    /**
     * GET ALL TASKS
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {

        List<TaskResponse> tasks = taskService.getAllTasks();

        return ResponseEntity.ok(
                ApiResponse.success(tasks)
        );
    }

    /**
     * GET TASK BY ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {

        TaskResponse task = taskService.getTaskById(id);

        return ResponseEntity.ok(
                ApiResponse.success(task)
        );
    }

    /**
     * UPDATE TASK
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse task = taskService.updateTask(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Task updated successfully", task)
        );
    }

    /**
     * DELETE TASK (SOFT DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @RequestParam Long deletedBy) {

        taskService.deleteTask(id, deletedBy);

        return ResponseEntity.ok(
                ApiResponse.success("Task deleted successfully", null)
        );
    }

    /**
     * RESTORE TASK
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TaskResponse>> restoreTask(@PathVariable Long id) {

        TaskResponse task = taskService.restoreTask(id);

        return ResponseEntity.ok(
                ApiResponse.success("Task restored successfully", task)
        );
    }

    /**
     * UPDATE TASK STATUS
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        TaskStatus newStatus = TaskStatus.valueOf((String) body.get("status"));
        Long updatedBy = Long.valueOf(body.get("updatedBy").toString());

        TaskResponse task = taskService.updateTaskStatus(id, newStatus, updatedBy);

        return ResponseEntity.ok(
                ApiResponse.success("Task status updated successfully", task)
        );
    }

    /**
     * ASSIGN TASK
     */
    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam Long updatedBy) {

        TaskResponse task = taskService.assignTask(id, userId, updatedBy);

        return ResponseEntity.ok(
                ApiResponse.success("Task assigned successfully", task)
        );
    }

    /**
     * GET TASKS BY PROJECT
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId) {

        List<TaskResponse> tasks = taskService.getTasksByProject(projectId);

        return ResponseEntity.ok(
                ApiResponse.success(tasks)
        );
    }

    /**
     * GET TASKS BY USER
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByUser(
            @PathVariable Long userId) {

        List<TaskResponse> tasks = taskService.getTasksByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(tasks)
        );
    }

    /**
     * GET TASKS BY STATUS
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByStatus(
            @PathVariable TaskStatus status) {

        List<TaskResponse> tasks = taskService.getTasksByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(tasks)
        );
    }
}