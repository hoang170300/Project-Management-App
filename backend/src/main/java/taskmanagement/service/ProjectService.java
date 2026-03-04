package taskmanagement.service;


import taskmanagement.dto.Request.ProjectRequest;
import taskmanagement.dto.Response.ProjectResponse;
import taskmanagement.enums.ProjectStatus;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest requestDTO);
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(Long id);
    ProjectResponse updateProject(Long id, ProjectRequest requestDTO);
    void deleteProject(Long id, Long deletedBy);
    ProjectResponse restoreProject(Long id);
    ProjectResponse updateProjectStatus(Long id, ProjectStatus newStatus, Long updatedBy);
}

