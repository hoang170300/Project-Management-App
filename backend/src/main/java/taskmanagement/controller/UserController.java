package taskmanagement.controller;

import taskmanagement.dto.Request.UserRequest;
import taskmanagement.dto.Response.UserResponse;
import taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "4. Users", description = "Quản lý User")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Tạo user mới")
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        log.info("REST request to create user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    @Operation(summary = "Danh sách user active")
    public ResponseEntity<List<UserResponse>> findAll() {
        log.info("REST request to get all users");
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết user theo ID")
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("REST request to get user: {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin user")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody UserRequest request) {
        log.info("REST request to update user: {}", id);
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete user")
    public ResponseEntity<Void> delete(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("REST request to delete user: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Khôi phục user đã xóa")
    public ResponseEntity<UserResponse> restore(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("REST request to restore user: {}", id);
        return ResponseEntity.ok(userService.restore(id));
    }
}