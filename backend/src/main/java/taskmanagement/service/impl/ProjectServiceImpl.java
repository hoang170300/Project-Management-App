package taskmanagement.service.impl;

import taskmanagement.dto.Request.ProjectRequest;
import taskmanagement.dto.Response.ProjectResponse;
import taskmanagement.entity.Project;
import taskmanagement.entity.User;
import taskmanagement.enums.ProjectStatus;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


        userRepository.findById(requestDTO.getCreatedBy())
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + requestDTO.getCreatedBy())
                );

        Project project = Project.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .status(requestDTO.getStatus() != null
                        ? requestDTO.getStatus()
                        : ProjectStatus.PLANNING)
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .createdBy(requestDTO.getCreatedBy())
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

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setStatus(requestDTO.getStatus());
        project.setStartDate(requestDTO.getStartDate());
        project.setEndDate(requestDTO.getEndDate());
        project.setUpdatedBy(requestDTO.getUpdatedBy());

        Project updatedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(updatedProject);
    }

    @Override
    public void deleteProject(Long id, Long deletedBy) {

        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id)
                );

        project.setDeleted(true);
        project.setDeletedBy(deletedBy);

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

        project.setStatus(newStatus);
        project.setUpdatedBy(updatedBy);

        Project updatedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(updatedProject);
    }
}