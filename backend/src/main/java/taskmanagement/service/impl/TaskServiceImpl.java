package taskmanagement.service.impl;

import taskmanagement.dto.Request.TaskRequest;
import taskmanagement.dto.Response.TaskResponse;
import taskmanagement.entity.Project;
import taskmanagement.entity.Task;
import taskmanagement.entity.User;
import taskmanagement.enums.TaskStatus;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.TaskService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public TaskResponse createTask(TaskRequest requestDTO) {

        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + requestDTO.getProjectId()));

        // NEW LOGIC: project must allow adding tasks
        if (!project.canAddTask()) {
            throw new RuntimeException(
                    "Cannot add tasks to project with status: " + project.getStatus()
            );
        }

        userRepository.findById(requestDTO.getCreatedBy())
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + requestDTO.getCreatedBy()));

        Task task = Task.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : TaskStatus.TODO)
                .priority(requestDTO.getPriority())
                .deadline(requestDTO.getDeadline())
                .project(project)
                .createdBy(requestDTO.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        if (requestDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(requestDTO.getAssigneeId())
                    .orElseThrow(() ->
                            new RuntimeException("Assignee not found with id: " + requestDTO.getAssigneeId()));

            if (assignee.getDeleted()) {
                throw new RuntimeException("Cannot assign task to deleted user");
            }

            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllActive()
                .stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + id));
        return TaskResponse.fromEntity(task);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest requestDTO) {

        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + id));

        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setPriority(requestDTO.getPriority());
        task.setDeadline(requestDTO.getDeadline());
        task.setUpdatedBy(requestDTO.getUpdatedBy());
        task.setUpdatedAt(LocalDateTime.now());

        // NEW LOGIC: update project if changed
        if (requestDTO.getProjectId() != null &&
                !requestDTO.getProjectId().equals(task.getProject().getId())) {

            Project newProject = projectRepository.findById(requestDTO.getProjectId())
                    .orElseThrow(() ->
                            new RuntimeException("Project not found with id: " + requestDTO.getProjectId()));

            if (!newProject.canAddTask()) {
                throw new RuntimeException(
                        "Cannot move task to project with status: " + newProject.getStatus()
                );
            }

            task.setProject(newProject);
        }

        if (requestDTO.getAssigneeId() != null) {
            User newAssignee = userRepository.findById(requestDTO.getAssigneeId())
                    .orElseThrow(() ->
                            new RuntimeException("Assignee not found with id: " + requestDTO.getAssigneeId()));

            if (newAssignee.getDeleted()) {
                throw new RuntimeException("Cannot assign task to deleted user");
            }

            task.setAssignee(newAssignee);
        }

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Override
    public void deleteTask(Long id, Long deletedBy) {

        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + id));

        // NEW LOGIC: prevent deleting DONE tasks
        if (task.getStatus() == TaskStatus.DONE) {
            throw new RuntimeException("Cannot delete tasks that are already DONE");
        }

        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setDeletedBy(deletedBy);

        taskRepository.save(task);
    }

    @Override
    public TaskResponse restoreTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + id));

        if (!task.getDeleted()) {
            throw new RuntimeException("Task is not deleted");
        }

        task.setDeleted(false);
        task.setDeletedAt(null);
        task.setDeletedBy(null);

        Task restoredTask = taskRepository.save(task);
        return TaskResponse.fromEntity(restoredTask);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus newStatus, Long updatedBy) {

        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + id));

        // NEW LOGIC: validate status transition
        if (!task.getStatus().canTransitionTo(newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition: " + task.getStatus() + " -> " + newStatus
            );
        }

        task.setStatus(newStatus);
        task.setUpdatedBy(updatedBy);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long userId, Long updatedBy) {

        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id: " + taskId));

        User assignee = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + userId));

        if (assignee.getDeleted()) {
            throw new RuntimeException("Cannot assign task to deleted user");
        }

        task.setAssignee(assignee);
        task.setUpdatedBy(updatedBy);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + projectId));

        return taskRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByUser(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + userId));

        return taskRepository.findByAssigneeIdAndDeletedFalse(userId)
                .stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {

        return taskRepository.findByStatusAndDeletedFalse(status)
                .stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }
}