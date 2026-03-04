package taskmanagement.repository;


import taskmanagement.entity.Project;
import taskmanagement.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE p.deleted = false")
    List<Project> findAllActive();

    Optional<Project> findByIdAndDeletedFalse(Long id);

    List<Project> findByStatusAndDeletedFalse(ProjectStatus status);

    List<Project> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
}
