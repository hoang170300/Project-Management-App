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

    // ── findById() ────────────────────────────────────────────────────────
    @Test
    @DisplayName("✅ findById: tìm thấy user active")
    void findById_Found() {
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.findById(1L);

        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@test.com");
        verify(userRepository, times(1)).findByIdActive(1L);
    }

    @Test
    @DisplayName("❌ findById: không tìm thấy → RuntimeException")
    void findById_NotFound_ThrowsException() {
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // ── create() ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("✅ create: thành công khi username và email chưa tồn tại")
    void create_Success() {
        UserRequest request = new UserRequest();
        request.setUsername("new_user");
        request.setEmail("new@test.com");
        request.setFullName("New User");
        request.setPassword("password123");

        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertThat(response.getUsername()).isEqualTo("new_user");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("❌ create: username đã tồn tại → RuntimeException")
    void create_DuplicateUsername_ThrowsException() {
        UserRequest request = new UserRequest();
        request.setUsername("john_doe");
        request.setEmail("other@test.com");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("john_doe");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ create: email đã tồn tại → RuntimeException")
    void create_DuplicateEmail_ThrowsException() {
        UserRequest request = new UserRequest();
        request.setUsername("another_user");
        request.setEmail("john@test.com");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("another_user")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("john@test.com");

        verify(userRepository, never()).save(any());
    }

    // ── delete() (soft delete) ────────────────────────────────────────────
    @Test
    @DisplayName("✅ delete: soft delete thành công")
    void delete_Success() {
        when(userRepository.findByIdActive(1L)).thenReturn(Optional.of(existingUser));

        userService.delete(1L);

        // Verify: save() được gọi sau khi softDelete()
        verify(userRepository).save(existingUser);
        assertThat(existingUser.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("❌ delete: user không tồn tại → RuntimeException")
    void delete_NotFound_ThrowsException() {
        when(userRepository.findByIdActive(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    // ── restore() ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("✅ restore: khôi phục user đã xóa thành công")
    void restore_Success() {
        existingUser.softDelete(); // đánh dấu đã xóa trước
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        UserResponse response = userService.restore(1L);

        assertThat(existingUser.isDeleted()).isFalse();
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("❌ restore: user chưa bị xóa → RuntimeException")
    void restore_NotDeleted_ThrowsException() {
        // existingUser.deleted = false (chưa xóa)
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.restore(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not deleted");
    }

    // ── findAll() ─────────────────────────────────────────────────────────
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