# TUẦN 4: TASK & PROJECT ENTITY

## Mục tiêu
- Mapping TaskEntity và ProjectEntity
- Xử lý JPA relationships
- Task & Project Service/Controller
- API filter tasks

---

## 1. Task Entity

```java
package com.example.taskmanagement.entity;

import com.example.taskmanagement.enums.TaskPriority;
import com.example.taskmanagement.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Title không được để trống")
    @Size(max = 200, message = "Title không quá 200 ký tự")
    private String title;
    
    @Column(length = 2000)
    @Size(max = 2000, message = "Description không quá 2000 ký tự")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
    
    @Column
    private LocalDate deadline;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Business logic
    public void updateStatus(TaskStatus newStatus) {
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("Không thể cập nhật task đã DONE");
        }
        this.status = newStatus;
    }
}
```

---

## 2. Project Entity

```java
package com.example.taskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Project name không được để trống")
    @Size(max = 200, message = "Project name không quá 200 ký tự")
    private String name;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Description không quá 1000 ký tự")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;
    
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
}
```

---

## 3. TaskRepository

```java
package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Tìm task theo project
    List<Task> findByProjectId(Long projectId);
    
    // Tìm task theo user
    List<Task> findByAssignedUserId(Long userId);
    
    // Tìm task theo status
    List<Task> findByStatus(TaskStatus status);
    
    // Tìm task theo project và status
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    // Tìm task theo user và status
    List<Task> findByAssignedUserIdAndStatus(Long userId, TaskStatus status);
    
    // Tìm task quá hạn
    @Query("SELECT t FROM Task t WHERE t.deadline < :currentDate AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);
    
    // Tìm task chưa assign
    List<Task> findByAssignedUserIsNull();
    
    // Đếm task theo project và status
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
}
```

---

## 4. ProjectRepository

```java
package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Tìm project theo manager
    List<Project> findByManagerId(Long managerId);
    
    // Tìm project theo status
    List<Project> findByStatus(String status);
    
    // Tìm project theo manager và status
    List<Project> findByManagerIdAndStatus(Long managerId, String status);
}
```

---

## 5. TaskService

```java
package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskCreateRequest;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.TaskStatus;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        log.info("Creating task: {}", request.getTitle());
        
        // Validate project exists
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        // Validate assigned user (if provided)
        User assignedUser = null;
        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .priority(request.getPriority())
                .project(project)
                .assignedUser(assignedUser)
                .deadline(request.getDeadline())
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created: {}", savedTask.getId());
        
        return toResponse(savedTask);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {
        log.info("Getting tasks for project: {}", projectId);
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUser(Long userId) {
        log.info("Getting tasks for user: {}", userId);
        return taskRepository.findByAssignedUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        task.updateStatus(newStatus);
        Task updated = taskRepository.save(task);
        
        return toResponse(updated);
    }
    
    @Transactional
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        task.setAssignedUser(user);
        Task updated = taskRepository.save(task);
        
        return toResponse(updated);
    }
    
    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .assignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : null)
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
```

---

## 6. TaskController

```java
package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.TaskCreateRequest;
import com.example.taskmanagement.dto.response.ApiResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.enums.TaskStatus;
import com.example.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TaskStatus status) {
        
        List<TaskResponse> tasks;
        
        if (projectId != null) {
            tasks = taskService.getTasksByProject(projectId);
        } else if (userId != null) {
            tasks = taskService.getTasksByUser(userId);
        } else if (status != null) {
            tasks = taskService.getTasksByStatus(status);
        } else {
            tasks = taskService.getAllTasks();
        }
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        TaskResponse response = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        TaskResponse response = taskService.assignTask(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## KẾT QUẢ TUẦN 4

✅ **Hoàn thành:**
- TaskEntity mapping với @ManyToOne relationships
- ProjectEntity mapping với @OneToMany
- Fix lazy/eager loading (sử dụng FetchType.LAZY)
- TaskRepository với custom queries (@Query)
- ProjectRepository
- TaskService với business logic
- ProjectService
- TaskController với filter APIs
- API list task theo user/project/status

📝 **Test APIs:**

```bash
# Tạo task
POST /api/tasks
{
  "title": "New Task",
  "projectId": 1,
  "assignedUserId": 2,
  "priority": "HIGH",
  "deadline": "2026-03-30"
}

# Lấy tasks theo project
GET /api/tasks?projectId=1

# Lấy tasks theo user
GET /api/tasks?userId=2

# Lấy tasks theo status
GET /api/tasks?status=IN_PROGRESS

# Update status
PUT /api/tasks/1/status?status=DONE

# Assign task
PUT /api/tasks/1/assign?userId=3
```
