package taskmanagement.service.impl;


import taskmanagement.dto.Response.UserResponse;
import taskmanagement.dto.Request.UserRequest;
import taskmanagement.entity.User;
import taskmanagement.repository.UserRepository;
import taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse create(UserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // Plain text for now, TUẦN 7 sẽ hash BCrypt
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.info("Finding user by ID: {}", id);
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.info("Finding all active users");
        return userRepository.findAllActive()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", id);

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting user with ID: {}", id);

        User user = userRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.softDelete();
        userRepository.save(user);

        log.info("User soft deleted successfully: {}", id);
    }

    @Override
    public UserResponse restore(Long id) {
        log.info("Restoring soft deleted user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (!user.getDeleted()) {
            throw new RuntimeException("User is not deleted: " + id);
        }
        user.restore();
        User restoredUser = userRepository.save(user);

        log.info("User restored successfully: {}", id);

        return UserResponse.fromEntity(restoredUser);
    }
}

