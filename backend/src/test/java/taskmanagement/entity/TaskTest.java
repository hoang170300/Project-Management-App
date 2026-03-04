package taskmanagement.entity;

import taskmanagement.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskWithDefaultStatusTodo() {
        Task task = new Task(
                "Test task",
                "Description",
                1L,
                1L
        );

        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Task(
                    "",
                    "Desc",
                    1L,
                    1L
            );
        });
    }

    @Test
    void shouldUpdateStatusFromTodoToInProgress() {
        Task task = new Task(
                "Test task",
                "Desc",
                1L,
                1L
        );

        task.updateStatus(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void shouldNotAllowDirectTodoToDone() {
        Task task = new Task(
                "Test task",
                "Desc",
                1L,
                1L
        );

        assertThrows(IllegalStateException.class, () -> {
            task.updateStatus(TaskStatus.DONE);
        });
    }
}