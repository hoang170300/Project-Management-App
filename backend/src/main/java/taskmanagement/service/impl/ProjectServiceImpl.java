package taskmanagement.service.impl;

import taskmanagement.dto.Request.ProjectRequest;
import taskmanagement.dto.Response.ProjectResponse;
import taskmanagement.entity.Project;
import taskmanagement.enums.ProjectStatus;
import taskmanagement.enums.TaskStatus;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.ProjectService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest requestDTO) {

        // Validate creator exists
        userRepository.findById(requestDTO.getCreatedBy())
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + requestDTO.getCreatedBy())
                );

        // Validate dates
        if (requestDTO.getStartDate() != null && requestDTO.getEndDate() != null) {
            if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
                throw new RuntimeException("End date cannot be before start date");
            }
        }

        Project project = Project.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus() != null
                        ? requestDTO.getStatus()
                        : ProjectStatus.PLANNING)
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .createdBy(requestDTO.getCreatedBy())
                .createdAt(LocalDateTime.now())   // thêm
                .deleted(false)
                .build();

        Project savedProject = projectRepository.save(project);

        return ProjectResponse.fromEntity(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {

        return projectRepository.findAllActive()
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {

        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        return ProjectResponse.fromEntity(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest requestDTO) {

        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        // Validate dates
        if (requestDTO.getStartDate() != null && requestDTO.getEndDate() != null) {
            if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
                throw new RuntimeException("End date cannot be before start date");
            }
        }

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setStatus(requestDTO.getStatus());
        project.setStartDate(requestDTO.getStartDate());
        project.setEndDate(requestDTO.getEndDate());
        project.setUpdatedBy(requestDTO.getUpdatedBy());
        project.setUpdatedAt(LocalDateTime.now()); // thêm

        Project updatedProject = projectRepository.save(project);

        return ProjectResponse.fromEntity(updatedProject);
    }

    @Override
    public void deleteProject(Long id, Long deletedBy) {

        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        // Business rule: cannot delete project with active tasks
        long activeTasks = project.getTasks()
                .stream()
                .filter(task -> !task.getDeleted())
                .count();

        if (activeTasks > 0) {
            throw new RuntimeException(
                    "Cannot delete project with active tasks. Found " + activeTasks + " active tasks."
            );
        }

        project.setDeleted(true);
        project.setDeletedBy(deletedBy);
        project.setDeletedAt(LocalDateTime.now()); // thêm

        projectRepository.save(project);
    }

    @Override
    public ProjectResponse restoreProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        if (!project.getDeleted()) {
            throw new RuntimeException("Project is not deleted");
        }

        project.setDeleted(false);
        project.setDeletedBy(null);
        project.setDeletedAt(null);

        Project restoredProject = projectRepository.save(project);

        return ProjectResponse.fromEntity(restoredProject);
    }

    @Override
    public ProjectResponse updateProjectStatus(Long id,
                                               ProjectStatus newStatus,
                                               Long updatedBy) {

        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        // Business rule: cannot change ACTIVE -> PLANNING
        if (project.getStatus() == ProjectStatus.ACTIVE
                && newStatus == ProjectStatus.PLANNING) {

            throw new RuntimeException(
                    "Cannot change project status back to PLANNING once it is ACTIVE"
            );
        }

        // Business rule: cannot complete if tasks not done
        if (newStatus == ProjectStatus.COMPLETED) {

            long incompleteTasks = project.getTasks()
                    .stream()
                    .filter(task ->
                            !task.getDeleted()
                                    && task.getStatus() != TaskStatus.DONE)
                    .count();

            if (incompleteTasks > 0) {
                throw new RuntimeException(
                        "Cannot complete project with incomplete tasks. Found "
                                + incompleteTasks + " incomplete tasks."
                );
            }
        }

        project.setStatus(newStatus);
        project.setUpdatedBy(updatedBy);
        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);

        return ProjectResponse.fromEntity(updatedProject);
    }
}