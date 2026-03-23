package taskmanagement.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import taskmanagement.enums.TaskStatus;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Task Entity Business Logic Tests")
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
    void isActive_NotDeleted_ReturnsTrue() {
        assertThat(task.isActive()).isTrue();
    }

    @Test
    @DisplayName("✅ Task đã soft delete → isActive() = false")
    void isActive_Deleted_ReturnsFalse() {
        task.setDeleted(true);
        assertThat(task.isActive()).isFalse();
    }

    @Test
    @DisplayName("✅ setStatus TODO → getStatus() = TODO")
    void setStatus_Todo() {
        task.setStatus(TaskStatus.TODO);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("✅ setStatus IN_PROGRESS → getStatus() = IN_PROGRESS")
    void setStatus_InProgress() {
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("✅ setStatus DONE → getStatus() = DONE")
    void setStatus_Done() {
        task.setStatus(TaskStatus.DONE);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("✅ deadline quá khứ + status TODO → isOverdue() = true")
    void isOverdue_PastDeadline_NotDone_ReturnsTrue() {
        task.setDeadline(java.time.LocalDate.now().minusDays(1));
        task.setStatus(TaskStatus.TODO);
        assertThat(task.isOverdue()).isTrue();
    }

    @Test
    @DisplayName("✅ deadline quá khứ + status DONE → isOverdue() = false")
    void isOverdue_PastDeadline_Done_ReturnsFalse() {
        task.setDeadline(java.time.LocalDate.now().minusDays(1));
        task.setStatus(TaskStatus.DONE);
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("✅ deadline tương lai → isOverdue() = false")
    void isOverdue_FutureDeadline_ReturnsFalse() {
        task.setDeadline(java.time.LocalDate.now().plusDays(7));
        task.setStatus(TaskStatus.TODO);
        assertThat(task.isOverdue()).isFalse();
    }

    @Test
    @DisplayName("✅ không có deadline → isOverdue() = false")
    void isOverdue_NullDeadline_ReturnsFalse() {
        task.setDeadline(null);
        assertThat(task.isOverdue()).isFalse();
    }

     @Test
    @DisplayName("✅ 2 task cùng ID → equals() = true")
    void equals_SameId_AreEqual() {
        Task t2 = new Task();
        t2.setId(1L);
        assertThat(task).isEqualTo(t2);
    }

    @Test
    @DisplayName("✅ 2 task khác ID → equals() = false")
    void equals_DifferentId_NotEqual() {
        Task t2 = new Task();
        t2.setId(2L);
        assertThat(task).isNotEqualTo(t2);
    }
}