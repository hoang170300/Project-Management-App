package taskmanagement.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User(
                1L,
                "user01",
                "password123",
                "user@mail.com",
                "Test User"
        );

        assertEquals("user01", user.getUsername());
        assertEquals("user@mail.com", user.getEmail());
        assertFalse(user.isDeleted());
    }

    @Test
    void shouldThrowExceptionWhenUsernameTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        1L,
                        "ab",
                        "password123",
                        "test@mail.com",
                        "Test User"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenPasswordTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        1L,
                        "user01",
                        "123",
                        "test@mail.com",
                        "Test User"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        1L,
                        "user01",
                        "password123",
                        "invalid-email",
                        "Test User"
                )
        );
    }

    @Test
    void shouldAddManagerRole() {
        User user = new User(
                1L,
                "manager01",
                "password123",
                "manager@mail.com",
                "Manager"
        );

        user.addRole("MANAGER");

        assertTrue(user.isManager());
        assertTrue(user.hasRole("MANAGER"));
    }

    @Test
    void shouldNotAddDuplicateRole() {
        User user = new User(
                1L,
                "user01",
                "password123",
                "user@mail.com",
                "Test User"
        );

        user.addRole("USER");
        user.addRole("USER");

        assertEquals(1, user.getRoles().size());
    }

    @Test
    void shouldThrowExceptionWhenInvalidRole() {
        User user = new User(
                1L,
                "user01",
                "password123",
                "user@mail.com",
                "Test User"
        );

        assertThrows(IllegalArgumentException.class, () ->
                user.addRole("ADMIN")
        );
    }

    @Test
    void shouldSoftDeleteUser() {
        User user = new User(
                1L,
                "user01",
                "password123",
                "user@mail.com",
                "Test User"
        );

        user.softDelete();

        assertTrue(user.isDeleted());
    }
}