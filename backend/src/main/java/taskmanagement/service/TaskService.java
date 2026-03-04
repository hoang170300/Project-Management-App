package taskmanagement.service;


import taskmanagement.dto.Request.TaskRequest;
import taskmanagement.dto.Response.TaskResponse;
import taskmanagement.enums.TaskStatus;

import java.util.List;


public interface TaskService {

    TaskResponse createTask(TaskRequest requestDTO);
    List<TaskResponse> getAllTasks();
    TaskResponse getTaskById(Long id);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id, Long deletedBy);
    TaskResponse restoreTask(Long id);
    TaskResponse updateTaskStatus(Long id, TaskStatus newStatus, Long updatedBy);
    TaskResponse assignTask(Long taskId, Long userId, Long updatedBy);
    List<TaskResponse> getTasksByProject(Long projectId);
    List<TaskResponse> getTasksByUser(Long userId);
    List<TaskResponse> getTasksByStatus(TaskStatus status);
}

