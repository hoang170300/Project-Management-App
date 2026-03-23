package taskmanagement.controller;

import taskmanagement.dto.Request.ProjectRequest;
import taskmanagement.dto.Response.ProjectResponse;
import taskmanagement.enums.ProjectStatus;
import taskmanagement.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "2. Projects", description = "Quản lý Project")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Tạo project mới")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Danh sách tất cả project")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết project theo ID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin project")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Cập nhật status project",
            description = "Body: {\"status\": \"ACTIVE\", \"updatedBy\": 1}\n\nFlow: PLANNING → ACTIVE → COMPLETED | CANCELLED"
    )
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        ProjectStatus newStatus = ProjectStatus.valueOf((String) body.get("status"));
        Long updatedBy = Long.valueOf(body.get("updatedBy").toString());
        return ResponseEntity.ok(projectService.updateProjectStatus(id, newStatus, updatedBy));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete project", description = "?deletedBy=1")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @RequestParam Long deletedBy) {
        projectService.deleteProject(id, deletedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Khôi phục project đã xóa")
    public ResponseEntity<ProjectResponse> restoreProject(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectService.restoreProject(id));
    }
}