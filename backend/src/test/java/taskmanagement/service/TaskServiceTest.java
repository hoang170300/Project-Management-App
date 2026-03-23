package taskmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.dto.Request.TaskRequest;
import taskmanagement.dto.Response.TaskResponse;
import taskmanagement.entity.Project;
import taskmanagement.entity.Task;
import taskmanagement.entity.User;
import taskmanagement.enums.Priority;
import taskmanagement.enums.ProjectStatus;
import taskmanagement.enums.TaskStatus;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.impl.TaskServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User manager;
    private User member;
    private User outsider;
    private Project activeProject;
    private Project completedProject;
    private Task todoTask;
    private Task inProgressTask;
    private Task doneTask;

    @BeforeEach
    void setUp() {
        // Users
        manager = new User();
        manager.setId(1L);
        manager.setUsername("manager");
        manager.setEmail("manager@test.com");
        manager.setFullName("Manager");
        manager.setDeleted(false);
        manager.setRoles(new HashSet<>());
        manager.addRole("MANAGER");

        member = new User();
        member.setId(2L);
        member.setUsername("member");
        member.setEmail("member@test.com");
        member.setFullName("Member");
        member.setDeleted(false);
        member.setRoles(new HashSet<>());
        member.addRole("USER");

        outsider = new User();
        outsider.setId(99L);
        outsider.setUsername("outsider");
        outsider.setEmail("out@test.com");
        outsider.setFullName("Outsider");
        outsider.setDeleted(false);
        outsider.setRoles(new HashSet<>());

        // Projects
        activeProject = new Project();
        activeProject.setId(10L);
        activeProject.setName("Active Project");
        activeProject.setStatus(ProjectStatus.ACTIVE);
        activeProject.setCreatedBy(1L);
        activeProject.setDeleted(false);

        completedProject = new Project();
        completedProject.setId(11L);
        completedProject.setName("Completed Project");
        completedProject.setStatus(ProjectStatus.COMPLETED);
        completedProject.setCreatedBy(1L);
        completedProject.setDeleted(false);

        // Tasks
        todoTask = new Task();
        todoTask.setId(100L);
        todoTask.setTitle("Todo Task");
        todoTask.setStatus(TaskStatus.TODO);
        todoTask.setProject(activeProject);
        todoTask.setAssignee(member);
        todoTask.setCreatedBy(1L);
        todoTask.setCreatedAt(LocalDateTime.now());
        todoTask.setUpdatedAt(LocalDateTime.now());
        todoTask.setDeleted(false);
        todoTask.setPriority(Priority.MEDIUM);

        inProgressTask = new Task();
        inProgressTask.setId(101L);
        inProgressTask.setTitle("InProgress Task");
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        inProgressTask.setProject(activeProject);
        inProgressTask.setAssignee(member);
        inProgressTask.setCreatedBy(1L);
        inProgressTask.setCreatedAt(LocalDateTime.now());
        inProgressTask.setUpdatedAt(LocalDateTime.now());
        inProgressTask.setDeleted(false);
        inProgressTask.setPriority(Priority.MEDIUM);

        doneTask = new Task();
        doneTask.setId(102L);
        doneTask.setTitle("Done Task");
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setProject(activeProject);
        doneTask.setAssignee(member);
        doneTask.setCreatedBy(1L);
        doneTask.setCreatedAt(LocalDateTime.now());
        doneTask.setUpdatedAt(LocalDateTime.now());
        doneTask.setDeleted(false);
        doneTask.setPriority(Priority.MEDIUM);
    }

    @Nested
    @DisplayName("createTask()")
    class CreateTask {

        private TaskRequest buildRequest(Long projectId, Long assigneeId) {
            TaskRequest req = new TaskRequest();
            req.setTitle("New Task");
            req.setDescription("Desc");
            req.setProjectId(projectId);
            req.setAssigneeId(assigneeId);
            req.setPriority(Priority.MEDIUM);
            req.setCreatedBy(1L);
            req.setDeadline(LocalDate.now().plusDays(7));
            return req;
        }

        @Test
        @DisplayName("✅ Tạo task thành công cho project ACTIVE")
        void createTask_Success() {
            TaskRequest request = buildRequest(10L, 2L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
            when(userRepository.findById(2L)).thenReturn(Optional.of(member));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                t.setId(999L);
                t.setCreatedAt(LocalDateTime.now());
                t.setUpdatedAt(LocalDateTime.now());
                return t;
            });

            TaskResponse response = taskService.createTask(request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("New Task");
            assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(response.getProjectId()).isEqualTo(10L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("❌ Project không tồn tại → exception")
        void createTask_ProjectNotFound() {
            TaskRequest request = buildRequest(999L, null);
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Project COMPLETED → canAddTask() = false → exception")
        void createTask_ProjectNotActive() {
            assertThat(completedProject.canAddTask()).isFalse();
            assertThat(activeProject.canAddTask()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateTaskStatus()")
    class UpdateTaskStatus {

        @Test
        @DisplayName("✅ TODO → IN_PROGRESS: hợp lệ")
        void updateStatus_TodoToInProgress() {
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));
            when(taskRepository.save(any())).thenReturn(todoTask);

            TaskResponse response = taskService.updateTaskStatus(100L, TaskStatus.IN_PROGRESS, 1L);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            verify(taskRepository).save(todoTask);
        }

        @Test
        @DisplayName("✅ IN_PROGRESS → DONE: hợp lệ")
        void updateStatus_InProgressToDone() {
            when(taskRepository.findByIdAndDeletedFalse(101L)).thenReturn(Optional.of(inProgressTask));
            when(taskRepository.save(any())).thenReturn(inProgressTask);

            TaskResponse response = taskService.updateTaskStatus(101L, TaskStatus.DONE, 1L);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("❌ Task không tồn tại → exception")
        void updateStatus_TaskNotFound() {
            when(taskRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTaskStatus(999L, TaskStatus.IN_PROGRESS, 1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getTasksByProject()")
    class GetTasksByProject {

        @Test
        @DisplayName("✅ Trả về list task theo projectId")
        void getTasksByProject_ReturnsList() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
            when(taskRepository.findByProjectIdAndDeletedFalse(10L))
                    .thenReturn(List.of(todoTask, inProgressTask, doneTask));

            List<TaskResponse> result = taskService.getTasksByProject(10L);

            assertThat(result).hasSize(3);
            verify(taskRepository).findByProjectIdAndDeletedFalse(10L);
        }

        @Test
        @DisplayName("✅ Không có task → trả về list rỗng")
        void getTasksByProject_EmptyList() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
            when(taskRepository.findByProjectIdAndDeletedFalse(10L)).thenReturn(List.of());

            List<TaskResponse> result = taskService.getTasksByProject(10L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("❌ Project không tồn tại → RuntimeException")
        void getTasksByProject_ProjectNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTasksByProject(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("getTasksByUser()")
    class GetTasksByUser {

        @Test
        @DisplayName("✅ Trả về task theo userId (assignee)")
        void getTasksByUser_ReturnsList() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(member));
            when(taskRepository.findByAssigneeIdAndDeletedFalse(2L))
                    .thenReturn(List.of(todoTask, inProgressTask));

            List<TaskResponse> result = taskService.getTasksByUser(2L);

            assertThat(result).hasSize(2);
            verify(taskRepository).findByAssigneeIdAndDeletedFalse(2L);
        }

        @Test
        @DisplayName("❌ User không tồn tại → RuntimeException")
        void getTasksByUser_UserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTasksByUser(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Project.canAddTask() business rule")
    class ProjectCanAddTask {

        @Test
        @DisplayName("✅ ACTIVE + not deleted → canAddTask() = true")
        void activeProject_CanAddTask() {
            assertThat(activeProject.canAddTask()).isTrue();
        }

        @Test
        @DisplayName("❌ COMPLETED → canAddTask() = false")
        void completedProject_CannotAddTask() {
            assertThat(completedProject.canAddTask()).isFalse();
        }

        @Test
        @DisplayName("❌ PLANNING → canAddTask() = false")
        void planningProject_CannotAddTask() {
            Project planning = new Project();
            planning.setStatus(ProjectStatus.PLANNING);
            planning.setDeleted(false);
            assertThat(planning.canAddTask()).isFalse();
        }

        @Test
        @DisplayName("❌ ACTIVE nhưng deleted = true → canAddTask() = false")
        void deletedProject_CannotAddTask() {
            activeProject.setDeleted(true);
            assertThat(activeProject.canAddTask()).isFalse();
        }
    }

    @Nested
    @DisplayName("assignTask()")
    class AssignTask {

        @Test
        @DisplayName("✅ Assign thành công cho user active")
        void assignTask_Success() {
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));
            when(userRepository.findById(2L)).thenReturn(Optional.of(member));
            when(taskRepository.save(any())).thenReturn(todoTask);

            TaskResponse response = taskService.assignTask(100L, 2L, 1L);

            assertThat(response.getAssigneeId()).isEqualTo(2L);
            verify(taskRepository).save(todoTask);
        }

        @Test
        @DisplayName("❌ Assign cho user đã bị xóa → RuntimeException")
        void assignTask_DeletedUser_ThrowsException() {
            outsider.softDelete();
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));
            when(userRepository.findById(99L)).thenReturn(Optional.of(outsider));

            assertThatThrownBy(() -> taskService.assignTask(100L, 99L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("deleted user");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteTask()")
    class DeleteTask {

        @Test
        @DisplayName("✅ Soft delete task TODO thành công")
        void deleteTask_TodoTask_Success() {
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));

            taskService.deleteTask(100L, 1L);

            assertThat(todoTask.getDeleted()).isTrue();
            assertThat(todoTask.getDeletedAt()).isNotNull();
            assertThat(todoTask.getDeletedBy()).isEqualTo(1L);
            verify(taskRepository).save(todoTask);
        }

        @Test
        @DisplayName("❌ Không được xóa task DONE → RuntimeException")
        void deleteTask_DoneTask_ThrowsException() {
            when(taskRepository.findByIdAndDeletedFalse(102L)).thenReturn(Optional.of(doneTask));

            assertThatThrownBy(() -> taskService.deleteTask(102L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DONE");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("restoreTask()")
    class RestoreTask {

        @Test
        @DisplayName("✅ Restore task đã xóa thành công")
        void restoreTask_Success() {
            todoTask.setDeleted(true);
            todoTask.setDeletedAt(LocalDateTime.now());
            when(taskRepository.findById(100L)).thenReturn(Optional.of(todoTask));
            when(taskRepository.save(any())).thenReturn(todoTask);

            taskService.restoreTask(100L);

            assertThat(todoTask.getDeleted()).isFalse();
            assertThat(todoTask.getDeletedAt()).isNull();
            verify(taskRepository).save(todoTask);
        }

        @Test
        @DisplayName("❌ Task chưa bị xóa → RuntimeException")
        void restoreTask_NotDeleted_ThrowsException() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(todoTask));

            assertThatThrownBy(() -> taskService.restoreTask(100L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not deleted");
        }
    }

    @Nested
    @DisplayName("TaskResponse.fromEntity()")
    class TaskResponseMapping {

        @Test
        @DisplayName("✅ Map đúng assigneeId từ task.getAssignee().getId()")
        void fromEntity_MapsAssigneeId() {
            TaskResponse response = TaskResponse.fromEntity(todoTask);

            assertThat(response.getAssigneeId()).isEqualTo(2L);
            assertThat(response.getAssigneeName()).isEqualTo("member");
        }

        @Test
        @DisplayName("✅ Task không có assignee → assigneeId = null")
        void fromEntity_NullAssignee() {
            todoTask.setAssignee(null);
            TaskResponse response = TaskResponse.fromEntity(todoTask);

            assertThat(response.getAssigneeId()).isNull();
            assertThat(response.getAssigneeName()).isNull();
        }

        @Test
        @DisplayName("✅ fromEntity(null) → trả về null")
        void fromEntity_Null() {
            assertThat(TaskResponse.fromEntity(null)).isNull();
        }

        @Test
        @DisplayName("✅ isOverdue được map đúng")
        void fromEntity_OverdueFlag() {
            todoTask.setDeadline(LocalDate.now().minusDays(1));
            todoTask.setStatus(TaskStatus.TODO);

            TaskResponse response = TaskResponse.fromEntity(todoTask);

            assertThat(response.getOverdue()).isTrue();
        }
    }
}