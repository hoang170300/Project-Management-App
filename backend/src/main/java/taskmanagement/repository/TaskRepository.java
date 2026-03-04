package taskmanagement.repository;

import taskmanagement.entity.Task;
import taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    List<Task> findAllActive();

    Optional<Task> findByIdAndDeletedFalse(Long id);
    List<Task> findByProjectIdAndDeletedFalse(Long projectId);

    List<Task> findByAssigneeIdAndDeletedFalse(Long assigneeId);

    List<Task> findByStatusAndDeletedFalse(TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = ?1 AND t.status = ?2 AND t.deleted = false")
    Long countByProjectAndStatus(Long projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.deadline < CURRENT_DATE AND t.status != 'DONE' AND t.deleted = false")
    List<Task> findOverdueTasks();
}

