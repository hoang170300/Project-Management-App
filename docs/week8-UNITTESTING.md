# TUẦN 8 - UNIT TESTING

## ✅ Mục tiêu tuần 8

- Viết Unit Test cho `TaskService` (createTask, updateTaskStatus, assignTask, deleteTask, restoreTask)
- Viết Unit Test cho `UserService` (create, findById, delete, restore)
- Mock Repository bằng Mockito
- Verify behavior với `verify()`
- Cover cả happy path lẫn error case

---

## 📦 Cấu trúc file test

```
src/test/java/taskmanagement/
├── service/
│   ├── TaskServiceTest.java      ← Test chính (nhiều case nhất)
│   └── UserServiceTest.java      ← Test user service
└── entity/
    ├── TaskTest.java             ← Test isActive, isOverdue, equals/hashCode
    └── UserTest.java             ← Test softDelete, restore, roles, equals/hashCode
```

---

## 1. Dependency (đã có sẵn trong pom.xml)

```xml
<!-- JUnit 5 + Mockito + AssertJ đã included sẵn -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 2. TaskServiceTest.java

```java
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

    // ══════════════════════════════════════════════════════════════════════
    // createTask
    // ══════════════════════════════════════════════════════════════════════
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
            // Service gọi findById(1L) cho createdBy trước, findById(2L) cho assignee sau
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
        @DisplayName("❌ Project không tồn tại → RuntimeException")
        void createTask_ProjectNotFound() {
            TaskRequest request = buildRequest(999L, null);
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Project COMPLETED → canAddTask()=false → RuntimeException")
        void createTask_ProjectNotActive() {
            // Kiểm tra trực tiếp business rule trên entity
            assertThat(completedProject.canAddTask()).isFalse();
            assertThat(activeProject.canAddTask()).isTrue();
        }

        @Test
        @DisplayName("❌ Assignee bị soft delete → RuntimeException")
        void createTask_DeletedAssignee() {
            outsider.softDelete();
            TaskRequest request = buildRequest(10L, 99L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
            when(userRepository.findById(99L)).thenReturn(Optional.of(outsider));

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("deleted user");

            verify(taskRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateTaskStatus — dùng canTransitionTo() trên TaskStatus enum
    // ══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateTaskStatus()")
    class UpdateTaskStatus {

        @Test
        @DisplayName("✅ TODO → IN_PROGRESS: hợp lệ")
        void todoToInProgress_Valid() {
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));
            when(taskRepository.save(any())).thenReturn(todoTask);

            TaskResponse response = taskService.updateTaskStatus(100L, TaskStatus.IN_PROGRESS, 1L);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(todoTask.getUpdatedBy()).isEqualTo(1L);
            verify(taskRepository).save(todoTask);
        }

        @Test
        @DisplayName("✅ IN_PROGRESS → DONE: hợp lệ")
        void inProgressToDone_Valid() {
            when(taskRepository.findByIdAndDeletedFalse(101L)).thenReturn(Optional.of(inProgressTask));
            when(taskRepository.save(any())).thenReturn(inProgressTask);

            TaskResponse response = taskService.updateTaskStatus(101L, TaskStatus.DONE, 1L);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("❌ TODO → DONE: skip bước → RuntimeException")
        void todoToDone_Invalid() {
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));

            assertThatThrownBy(() -> taskService.updateTaskStatus(100L, TaskStatus.DONE, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid status transition");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Task không tồn tại → RuntimeException")
        void updateStatus_TaskNotFound() {
            when(taskRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTaskStatus(999L, TaskStatus.IN_PROGRESS, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // assignTask
    // ══════════════════════════════════════════════════════════════════════
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
        @DisplayName("❌ Assign cho user đã soft delete → RuntimeException")
        void assignTask_DeletedUser() {
            outsider.softDelete();
            when(taskRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(todoTask));
            when(userRepository.findById(99L)).thenReturn(Optional.of(outsider));

            assertThatThrownBy(() -> taskService.assignTask(100L, 99L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("deleted user");

            verify(taskRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // deleteTask (soft delete)
    // ══════════════════════════════════════════════════════════════════════
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
        void deleteTask_DoneTask_Blocked() {
            when(taskRepository.findByIdAndDeletedFalse(102L)).thenReturn(Optional.of(doneTask));

            assertThatThrownBy(() -> taskService.deleteTask(102L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DONE");

            verify(taskRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // restoreTask
    // ══════════════════════════════════════════════════════════════════════
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
        void restoreTask_NotDeleted() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(todoTask));

            assertThatThrownBy(() -> taskService.restoreTask(100L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not deleted");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getTasksByProject
    // ══════════════════════════════════════════════════════════════════════
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
        @DisplayName("✅ Project không có task → list rỗng")
        void getTasksByProject_EmptyList() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
            when(taskRepository.findByProjectIdAndDeletedFalse(10L)).thenReturn(List.of());

            assertThat(taskService.getTasksByProject(10L)).isEmpty();
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

    // ══════════════════════════════════════════════════════════════════════
    // getTasksByUser
    // ══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getTasksByUser()")
    class GetTasksByUser {

        @Test
        @DisplayName("✅ Trả về task theo userId")
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

    // ══════════════════════════════════════════════════════════════════════
    // TaskResponse.fromEntity() — mapping
    // ══════════════════════════════════════════════════════════════════════
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
            assertThat(TaskResponse.fromEntity(todoTask).getAssigneeId()).isNull();
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
            assertThat(TaskResponse.fromEntity(todoTask).getOverdue()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Project.canAddTask() — business rule
    // ══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Project.canAddTask()")
    class ProjectCanAddTask {

        @Test
        @DisplayName("✅ ACTIVE + not deleted → true")
        void activeProject_CanAddTask() {
            assertThat(activeProject.canAddTask()).isTrue();
        }

        @Test
        @DisplayName("❌ COMPLETED → false")
        void completedProject_Cannot() {
            assertThat(completedProject.canAddTask()).isFalse();
        }

        @Test
        @DisplayName("❌ PLANNING → false")
        void planningProject_Cannot() {
            Project p = new Project();
            p.setStatus(ProjectStatus.PLANNING);
            p.setDeleted(false);
            assertThat(p.canAddTask()).isFalse();
        }

        @Test
        @DisplayName("❌ ACTIVE nhưng deleted=true → false")
        void deletedActiveProject_Cannot() {
            activeProject.setDeleted(true);
            assertThat(activeProject.canAddTask()).isFalse();
        }
    }
}
```

---

## 3. UserServiceTest.java

```java
package taskmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.dto.Request.UserRequest;
import taskmanagement.dto.Response.UserResponse;
import taskmanagement.entity.User;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.impl.UserServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("john_doe");
        existingUser.setEmail("john@test.com");
        existingUser.setFullName("John Doe");
        existingUser.setPassword("password123");
        existingUser.setDeleted(false);
        existingUser.setRoles(new HashSet<>());
    }

    @Test
    @DisplayName("✅ findById: tìm thấy user active")
    void findById_Found() {
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.findById(1L);

        assertThat(response.getUsername()).isEqualTo("john_doe");
        verify(userRepository, times(1)).findByIdActive(1L);
    }

    @Test
    @DisplayName("❌ findById: không tìm thấy → RuntimeException")
    void findById_NotFound() {
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("✅ create: thành công")
    void create_Success() {
        UserRequest req = new UserRequest();
        req.setUsername("new_user");
        req.setEmail("new@test.com");
        req.setFullName("New User");
        req.setPassword("password123");

        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = userService.create(req);

        assertThat(response.getUsername()).isEqualTo("new_user");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("❌ create: username đã tồn tại → RuntimeException")
    void create_DuplicateUsername() {
        UserRequest req = new UserRequest();
        req.setUsername("john_doe");
        req.setEmail("other@test.com");
        req.setPassword("pass");

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("john_doe");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ create: email đã tồn tại → RuntimeException")
    void create_DuplicateEmail() {
        UserRequest req = new UserRequest();
        req.setUsername("another");
        req.setEmail("john@test.com");
        req.setPassword("pass");

        when(userRepository.existsByUsername("another")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("john@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ delete: soft delete thành công")
    void delete_Success() {
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(existingUser));

        userService.delete(1L);

        assertThat(existingUser.isDeleted()).isTrue();
        assertThat(existingUser.getDeletedAt()).isNotNull();
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("❌ delete: user không tồn tại → RuntimeException")
    void delete_NotFound() {
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ restore: khôi phục user đã xóa")
    void restore_Success() {
        existingUser.softDelete();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        userService.restore(1L);

        assertThat(existingUser.isDeleted()).isFalse();
        assertThat(existingUser.getDeletedAt()).isNull();
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("❌ restore: user chưa bị xóa → RuntimeException")
    void restore_NotDeleted() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.restore(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not deleted");
    }

    @Test
    @DisplayName("✅ findAll: trả về danh sách user active")
    void findAll_ReturnsList() {
        when(userRepository.findAllActive()).thenReturn(List.of(existingUser));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("john_doe");
        verify(userRepository).findAllActive();
    }
}
```

---

## 4. TaskTest.java

```java
package taskmanagement.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import taskmanagement.enums.TaskStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Task Entity Tests")
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);
        task.setDeleted(false);
    }

    @Test
    @DisplayName("✅ Task chưa xóa → isActive() = true")
    void isActive_NotDeleted() {
        assertThat(task.isActive()).isTrue();
    }

    @Test
    @DisplayName("✅ Task đã soft delete → isActive() = false")
    void isActive_Deleted() {
        task.setDeleted(true);
        assertThat(task.isActive()).isFalse();
    }

    @Test
    @DisplayName("✅ deadline quá khứ + TODO → isOverdue() = true")
    void isOverdue_Past_NotDone() {
        task.setDeadline(LocalDate.now().minusDays(1));
        assertThat(task.isOverdue()).isTrue();
    }

    @Test
    @DisplayName("✅ deadline quá khứ + DONE → isOverdue() = false")
    void isOverdue_Past_Done() {
        task.setDeadline(LocalDate.now().minusDays(1));
        task.setStatus(TaskStatus.DONE);
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("✅ deadline tương lai → isOverdue() = false")
    void isOverdue_Future() {
        task.setDeadline(LocalDate.now().plusDays(7));
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("✅ deadline null → isOverdue() = false")
    void isOverdue_Null() {
        task.setDeadline(null);
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("✅ 2 task cùng ID → equals() = true")
    void equals_SameId() {
        Task t2 = new Task();
        t2.setId(1L);
        assertThat(task).isEqualTo(t2);
    }

    @Test
    @DisplayName("✅ 2 task khác ID → equals() = false")
    void equals_DifferentId() {
        Task t2 = new Task();
        t2.setId(2L);
        assertThat(task).isNotEqualTo(t2);
    }
}
```

---

## 5. UserTest.java

```java
package taskmanagement.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("✅ 2 user cùng ID → equals()=true, Set.size()=1")
    void equals_SameId() {
        User u1 = new User(); u1.setId(1L); u1.setUsername("john");
        User u2 = new User(); u2.setId(1L); u2.setUsername("john");

        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());

        Set<User> set = new HashSet<>();
        set.add(u1); set.add(u2);
        assertThat(set).hasSize(1);
    }

    @Test
    @DisplayName("✅ 2 user khác ID → equals() = false")
    void equals_DifferentId() {
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);
        assertThat(u1).isNotEqualTo(u2);
    }

    @Test
    @DisplayName("✅ ID null → so sánh theo username")
    void equals_NullId_ByUsername() {
        User u1 = new User(); u1.setUsername("john");
        User u2 = new User(); u2.setUsername("john");
        assertThat(u1).isEqualTo(u2);
    }

    @Test
    @DisplayName("✅ softDelete() → isDeleted()=true, deletedAt!=null")
    void softDelete() {
        User u = new User(); u.setId(1L); u.setUsername("test"); u.setDeleted(false);
        u.softDelete();

        assertThat(u.isDeleted()).isTrue();
        assertThat(u.isActive()).isFalse();
        assertThat(u.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("✅ restore() → isDeleted()=false, deletedAt=null")
    void restore() {
        User u = new User(); u.setId(1L); u.setUsername("test"); u.setDeleted(false);
        u.softDelete();
        u.restore();

        assertThat(u.isDeleted()).isFalse();
        assertThat(u.isActive()).isTrue();
        assertThat(u.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("✅ addRole(MANAGER) → isManager() = true")
    void addRole_Manager() {
        User u = new User(); u.setRoles(new HashSet<>());
        u.addRole("MANAGER");
        assertThat(u.isManager()).isTrue();
    }

    @Test
    @DisplayName("✅ addRole(USER) → isManager() = false")
    void addRole_User() {
        User u = new User(); u.setRoles(new HashSet<>());
        u.addRole("USER");
        assertThat(u.isManager()).isFalse();
    }

    @Test
    @DisplayName("❌ addRole invalid → IllegalArgumentException")
    void addRole_Invalid() {
        User u = new User(); u.setRoles(new HashSet<>());
        assertThatThrownBy(() -> u.addRole("ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }
}
```

---

## 6. Chạy test

```bash
# Chạy toàn bộ
mvn test

# Kết quả mong đợi
# Tests run: XX, Failures: 0, Errors: 0
# Process finished with exit code 0  ✅

# Chạy từng class
mvn test -Dtest=TaskServiceTest
mvn test -Dtest=UserServiceTest
mvn test -Dtest=TaskTest
mvn test -Dtest=UserTest
```

---

## 7. Checklist tuần 8

| Task | File | Status |
|------|------|--------|
| Test `createTask` (happy path + lỗi) | `TaskServiceTest` | ✅ |
| Test `updateTaskStatus` (valid + invalid transition) | `TaskServiceTest` | ✅ |
| Test `assignTask` (success + deleted user) | `TaskServiceTest` | ✅ |
| Test `deleteTask` (success + DONE blocked) | `TaskServiceTest` | ✅ |
| Test `restoreTask` (success + not deleted) | `TaskServiceTest` | ✅ |
| Test `getTasksByProject` / `getTasksByUser` | `TaskServiceTest` | ✅ |
| Test `Project.canAddTask()` business rule | `TaskServiceTest` | ✅ |
| Mock Repository bằng Mockito | Tất cả | ✅ |
| Verify behavior (`verify()`, `never()`) | Tất cả | ✅ |
| Test `isActive`, `isOverdue`, `equals` | `TaskTest` | ✅ |
| Test `softDelete`, `restore`, `roles` | `UserTest` | ✅ |
| Run & fix → exit code 0 | `mvn test` | ✅ |
| Commit | Git | ▶️ |

---

## 💡 Ghi nhớ quan trọng

```
@Mock              → giả lập dependency (Repository)
@InjectMocks       → inject mock vào class đang test (ServiceImpl)

when(x).thenReturn(y)      → stub thông thường
when(x).thenAnswer(inv->)  → stub động (cần xử lý logic)
thenReturn(Optional.empty()) → giả lập không tìm thấy

verify(mock).method()           → phải được gọi
verify(mock, never()).method()  → KHÔNG được gọi
verify(mock, times(n)).method() → gọi đúng n lần

assertThat(x).isEqualTo(y)
assertThat(x).isTrue() / isFalse()
assertThat(list).hasSize(n)
assertThatThrownBy(() -> ...)
    .isInstanceOf(X.class)
    .hasMessageContaining("...")
```

---

**Người thực hiện:** Ngo Viet Hoang  
**Tuần:** 8/10  
**Next:** Tuần 9 — Deploy + Swagger UI
