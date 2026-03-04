# TUẦN 3: SPRING BOOT - USER CRUD

## Mục tiêu
- Khởi tạo Spring Boot project
- Cấu trúc package chuẩn
- Kết nối SQL Server
- Implement User CRUD API

---

## 1. Khởi tạo Spring Boot Project

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>task-management</artifactId>
    <version>1.0.0</version>
    <name>Task Management System</name>
    <description>Task Management with Spring Boot and SQL Server</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- SQL Server Driver -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 2. Cấu trúc Package chuẩn

```
src/main/java/com/example/taskmanagement/
├── TaskManagementApplication.java
├── config/
│   └── JpaConfig.java
├── entity/
│   ├── User.java
│   ├── Task.java
│   └── Project.java
├── enums/
│   ├── UserRole.java
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── repository/
│   ├── UserRepository.java
│   ├── TaskRepository.java
│   └── ProjectRepository.java
├── service/
│   ├── UserService.java
│   ├── TaskService.java
│   └── ProjectService.java
├── controller/
│   ├── UserController.java
│   ├── TaskController.java
│   └── ProjectController.java
├── dto/
│   ├── request/
│   │   ├── UserCreateRequest.java
│   │   └── UserUpdateRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── ApiResponse.java
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## 3. Cấu hình Database

### application.yml

```yaml
spring:
  application:
    name: task-management-system
  
  # SQL Server Configuration
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=TaskManagementDB;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourPassword123
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: update  # update: tự động cập nhật schema (dùng cho dev)
      # ddl-auto: validate  # validate: chỉ kiểm tra schema (dùng cho prod)
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServerDialect
        format_sql: true
    open-in-view: false

# Logging
logging:
  level:
    com.example.taskmanagement: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api
```

### application-dev.yml (Profile cho Development)

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=TaskManagementDB;encrypt=true;trustServerCertificate=true
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.example.taskmanagement: DEBUG
```

### application-prod.yml (Profile cho Production)

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://prod-server:1433;databaseName=TaskManagementDB;encrypt=true;trustServerCertificate=false
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.example.taskmanagement: INFO
```

---

## 4. User Entity với JPA

### User.java

```java
package com.example.taskmanagement.entity;

import com.example.taskmanagement.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;
    
    @Column(nullable = false)
    @NotBlank(message = "Password không được để trống")
    private String password;
    
    @Column(name = "full_name", nullable = false, length = 100)
    @NotBlank(message = "Full name không được để trống")
    @Size(max = 100, message = "Full name không quá 100 ký tự")
    private String fullName;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## 5. UserRepository

### UserRepository.java

```java
package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra username đã tồn tại
    boolean existsByUsername(String username);
    
    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);
    
    // Tìm user theo role
    List<User> findByRole(UserRole role);
}
```

---

## 6. DTOs (Data Transfer Objects)

### UserCreateRequest.java

```java
package com.example.taskmanagement.dto.request;

import com.example.taskmanagement.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserCreateRequest {
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;
    
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải ít nhất 6 ký tự")
    private String password;
    
    @NotBlank(message = "Full name không được để trống")
    @Size(max = 100, message = "Full name không quá 100 ký tự")
    private String fullName;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    private UserRole role = UserRole.USER;
}
```

### UserResponse.java

```java
package com.example.taskmanagement.dto.response;

import com.example.taskmanagement.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
}
```

### ApiResponse.java

```java
package com.example.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

---

## 7. UserService

### UserService.java

```java
package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.UserCreateRequest;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {}", request.getUsername());
        
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại: " + request.getUsername());
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }
        
        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // TODO: Hash password
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());
        
        return toResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UserCreateRequest request) {
        log.info("Updating user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Kiểm tra username conflict (nếu thay đổi)
        if (!user.getUsername().equals(request.getUsername()) 
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại: " + request.getUsername());
        }
        
        // Update fields
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);
        
        return toResponse(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
    
    // Helper: Entity → DTO
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
```

---

## 8. UserController (REST API)

### UserController.java

```java
package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.UserCreateRequest;
import com.example.taskmanagement.dto.response.ApiResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * POST /api/users - Tạo user mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("POST /api/users - Creating user: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
    
    /**
     * GET /api/users - Lấy danh sách user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("GET /api/users - Getting all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    /**
     * GET /api/users/{id} - Lấy user theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Getting user by id", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    /**
     * PUT /api/users/{id} - Cập nhật user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateRequest request) {
        log.info("PUT /api/users/{} - Updating user", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * DELETE /api/users/{id} - Xóa user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("User deleted successfully")
                .build());
    }
}
```

---

## 9. Exception Handling

### ResourceNotFoundException.java

```java
package com.example.taskmanagement.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler.java

```java
package com.example.taskmanagement.exception;

import com.example.taskmanagement.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Xử lý ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }
    
    /**
     * Xử lý IllegalArgumentException (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }
    
    /**
     * Xử lý Validation errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }
    
    /**
     * Xử lý tất cả exception khác (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Internal server error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

---

## 10. Main Application

### TaskManagementApplication.java

```java
package com.example.taskmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }
}
```

---

## 11. Test với Postman

### Tạo User (POST)

```
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "USER"
}
```

**Response (201 Created):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2026-03-02T10:30:00"
  }
}
```

### Lấy tất cả Users (GET)

```
GET http://localhost:8080/api/users
```

### Lấy User theo ID (GET)

```
GET http://localhost:8080/api/users/1
```

### Cập nhật User (PUT)

```
PUT http://localhost:8080/api/users/1
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "fullName": "John Doe Updated",
  "email": "john@example.com",
  "role": "MANAGER"
}
```

### Xóa User (DELETE)

```
DELETE http://localhost:8080/api/users/1
```

---

## KẾT QUẢ TUẦN 3

✅ **Hoàn thành:**
- Khởi tạo Spring Boot project với Maven
- Cấu trúc package chuẩn (entity, repository, service, controller, dto, exception)
- Cấu hình kết nối SQL Server
- Log startup thành công
- UserEntity với JPA annotations
- UserRepository interface
- UserService với business logic
- UserController với REST API CRUD
- Test API User bằng Postman (200 OK)
- Exception handling cơ bản
- Refactor naming theo convention
- Commit + ghi chú Git

✅ **Files tạo ra:**
- `pom.xml`
- `application.yml`
- `User.java` (Entity)
- `UserRepository.java`
- `UserService.java`
- `UserController.java`
- DTOs: `UserCreateRequest`, `UserResponse`, `ApiResponse`
- Exception: `ResourceNotFoundException`, `GlobalExceptionHandler`

📝 **Ghi chú:**
- Sử dụng Lombok để giảm boilerplate code
- @Transactional cho data consistency
- Validation với Bean Validation API
- RESTful API design
- Response chuẩn với ApiResponse wrapper
