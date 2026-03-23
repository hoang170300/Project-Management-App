# TUẦN 9 - DEPLOY + SWAGGER UI

## ✅ Mục tiêu tuần 9

- Tách profile `dev` / `prod`
- Build JAR & chạy local
- Tích hợp Swagger UI (SpringDoc OpenAPI)
- Document tất cả API + JWT auth header
- Viết README hướng dẫn setup & run

---

## 📦 File thay đổi tuần 9

```
src/main/
├── java/taskmanagement/
│   └── config/
│       └── OpenApiConfig.java          ← NEW
└── resources/
    ├── application.properties          ← UPDATE
    ├── application-dev.properties      ← NEW
    └── application-prod.properties     ← NEW
README.md                               ← NEW
```

---

## 1. Thêm dependency Swagger vào pom.xml

```xml
<!-- SpringDoc OpenAPI (Swagger UI) — Spring Boot 3.x -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

---

## 2. application.properties (base config)

```properties
spring.application.name=task-management-system
spring.profiles.active=dev

# SpringDoc / Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha

# Server
server.port=8089

# JWT
jwt.secret=YourVeryLongSecretKeyAtLeast256BitsForHS256AlgorithmHere
jwt.expiration=86400000
```

> ⚠️ Project dùng `@RequestMapping("/api/tasks")` trực tiếp trong Controller  
> nên **không** cần `server.servlet.context-path=/api`

---

## 3. application-dev.properties

```properties
# Profile: DEV — chạy local
spring.datasource.url=jdbc:sqlserver://Hoang;databaseName=TaskManagementDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123456yeO
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

logging.level.taskmanagement=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

## 4. application-prod.properties

```properties
# Profile: PROD — deploy server
# Đọc từ environment variable
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

logging.level.taskmanagement=INFO
logging.level.org.hibernate.SQL=WARN

# Tắt Swagger trong prod
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

---

## 5. OpenApiConfig.java

```java
package taskmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management API")
                        .version("1.0.0")
                        .description("""
                                REST API quản lý task theo project.
                                
                                **Hướng dẫn auth:**
                                1. Gọi `POST /api/auth/login` để lấy JWT token
                                2. Click nút **Authorize** (🔒) góc trên phải
                                3. Nhập: `Bearer <your_token>`
                                4. Tất cả request sau tự gắn header
                                """)
                        .contact(new Contact().name("Ngo Viet Hoang")))

                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

## 6. SecurityConfig.java — Thêm Swagger vào permitAll()

```java
// Thêm vào phần .authorizeHttpRequests() trong SecurityConfig hiện tại:
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/public/**").permitAll()

        // ← THÊM: Swagger không cần token
        .requestMatchers(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
        ).permitAll()

        .anyRequest().authenticated()
)
```

> CORS đã config sẵn trong SecurityConfig (`localhost:5174`, `localhost:3000`)  
> Swagger UI chạy trên cùng host nên không cần thêm origin.

---

## 7. Annotate Controllers

### AuthController.java

```java
package taskmanagement.controller;

import taskmanagement.dto.Response.ApiResponse;
import taskmanagement.dto.Request.LoginRequest;
import taskmanagement.dto.Request.RegisterRequest;
import taskmanagement.dto.Response.AuthResponse;
import taskmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "1. Authentication", description = "Đăng ký / Đăng nhập")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập → nhận JWT token")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
```

### TaskController.java

```java
package taskmanagement.controller;

import taskmanagement.dto.Response.ApiResponse;
import taskmanagement.dto.Request.TaskRequest;
import taskmanagement.dto.Response.TaskResponse;
import taskmanagement.enums.TaskStatus;
import taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "3. Tasks", description = "Quản lý Task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(
            summary = "Tạo task mới",
            description = "Project phải ACTIVE. createdBy = ID người tạo."
    )
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping
    @Operation(summary = "Danh sách tất cả task (active)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success(taskService.getAllTasks()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết task theo ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully",
                taskService.updateTask(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft delete task",
            description = "?deletedBy=1 | Không được xóa task DONE"
    )
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestParam Long deletedBy) {
        taskService.deleteTask(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Khôi phục task đã xóa")
    public ResponseEntity<ApiResponse<TaskResponse>> restoreTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task restored successfully",
                taskService.restoreTask(id)));
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Cập nhật status task",
            description = "Body: {\"status\": \"IN_PROGRESS\", \"updatedBy\": 1}\n\nFlow: TODO → IN_PROGRESS → DONE (không skip)"
    )
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        TaskStatus newStatus = TaskStatus.valueOf((String) body.get("status"));
        Long updatedBy = Long.valueOf(body.get("updatedBy").toString());
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully",
                taskService.updateTaskStatus(id, newStatus, updatedBy)));
    }

    @PutMapping("/{id}/assign/{userId}")
    @Operation(
            summary = "Gán task cho user",
            description = "?updatedBy=1 | User phải còn active (chưa bị soft delete)"
    )
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam Long updatedBy) {
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully",
                taskService.assignTask(id, userId, updatedBy)));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Danh sách task theo project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByProject(projectId)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Danh sách task theo user (assignee)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByUser(userId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Danh sách task theo status: TODO | IN_PROGRESS | DONE")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByStatus(
            @Parameter(description = "TaskStatus") @PathVariable TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByStatus(status)));
    }
}
```

### ProjectController.java

```java
package taskmanagement.controller;

import taskmanagement.dto.Request.ProjectRequest;
import taskmanagement.dto.Response.ProjectResponse;
import taskmanagement.enums.ProjectStatus;
import taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "2. Projects", description = "Quản lý Project")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Tạo project mới")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Danh sách tất cả project")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết project theo ID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin project")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Cập nhật status project",
            description = "Body: {\"status\": \"ACTIVE\", \"updatedBy\": 1}\n\nFlow: PLANNING → ACTIVE → COMPLETED | CANCELLED"
    )
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        ProjectStatus newStatus = ProjectStatus.valueOf((String) body.get("status"));
        Long updatedBy = Long.valueOf(body.get("updatedBy").toString());
        return ResponseEntity.ok(projectService.updateProjectStatus(id, newStatus, updatedBy));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete project", description = "?deletedBy=1")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @RequestParam Long deletedBy) {
        projectService.deleteProject(id, deletedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Khôi phục project đã xóa")
    public ResponseEntity<ProjectResponse> restoreProject(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        return ResponseEntity.ok(projectService.restoreProject(id));
    }
}
```

### UserController.java

```java
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
```

---

## 8. Build JAR & chạy

```bash
# ── Build ──────────────────────────────────────────────────
mvn clean package -DskipTests

# JAR: target/task-management-1.0.0.jar

# ── Chạy DEV (local) ───────────────────────────────────────
java -jar target/task-management-1.0.0.jar \
     --spring.profiles.active=dev

# Hoặc Maven plugin
mvn spring-boot:run

# ── Chạy PROD ──────────────────────────────────────────────
java -jar target/task-management-1.0.0.jar \
     --spring.profiles.active=prod \
     --DB_URL="jdbc:sqlserver://host:1433;databaseName=TaskDB;encrypt=true;trustServerCertificate=true" \
     --DB_USERNAME=sa \
     --DB_PASSWORD=SecurePass

# ── Verify chạy OK ─────────────────────────────────────────
curl -X POST http://localhost:8089/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin1","password":"123456"}'
```

---

## 9. Truy cập Swagger UI

```
http://localhost:8089/swagger-ui.html
```

**Hướng dẫn test qua Swagger:**
```
1. Mở http://localhost:8089/swagger-ui.html
2. Mục "1. Authentication" → POST /api/auth/login → Try it out → Execute
3. Copy token từ response.data.token
4. Click [Authorize 🔒] góc trên phải
5. Nhập: Bearer eyJ...  → Authorize
6. Từ giờ tất cả request tự gắn: Authorization: Bearer eyJ...
```

---

````markdown
# Task Management System

REST API quản lý task theo project — Spring Boot + SQL Server.

## Tech Stack
- Java 17, Spring Boot 3.2
- Spring Security + JWT (jjwt 0.12.3)
- SQL Server / Hibernate JPA
- Swagger UI (SpringDoc 2.3.0)
- CORS cho frontend Vue.js (port 5174/3000)

## Yêu cầu
- Java 17+, Maven 3.8+
- SQL Server (local hoặc Docker)

## Cách chạy

### 1. Clone & cấu hình DB
```bash
git clone <repo_url> && cd backend
```
Chỉnh `src/main/resources/application-dev.properties`:
```properties
spring.datasource.password=<your_password>
```

### 2. Chạy app
```bash
mvn spring-boot:run
```

### 3. Swagger UI
`http://localhost:8089/swagger-ui.html`

## API Endpoints

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | /api/auth/register | Đăng ký | ❌ |
| POST | /api/auth/login | Đăng nhập, lấy JWT | ❌ |
| GET | /api/users | Danh sách user | ✅ |
| POST | /api/projects | Tạo project | ✅ |
| GET | /api/projects | Danh sách project | ✅ |
| PUT | /api/projects/{id}/status | Cập nhật status project | ✅ |
| POST | /api/tasks | Tạo task | ✅ |
| GET | /api/tasks/project/{projectId} | Task theo project | ✅ |
| GET | /api/tasks/user/{userId} | Task theo user | ✅ |
| GET | /api/tasks/status/{status} | Task theo status | ✅ |
| PUT | /api/tasks/{id}/status | Cập nhật status task | ✅ |
| PUT | /api/tasks/{id}/assign/{userId} | Gán task | ✅ |
| DELETE | /api/tasks/{id} | Soft delete task | ✅ |
| POST | /api/tasks/{id}/restore | Restore task | ✅ |

## Business Rules
- Status task: `TODO → IN_PROGRESS → DONE` (không skip)
- Chỉ tạo task cho project **ACTIVE**
- Không xóa task đã **DONE**
- Không assign cho user đã bị soft delete

## Build Production
```bash
mvn clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod
```
````

---

## 10. Checklist tuần 9

| Task | File | Status |
|------|------|--------|
| Tách profile dev / prod | `application-dev/prod.properties` | ✅ |
| Build JAR thành công | `mvn clean package` | ▶️ |
| Chạy JAR local | `java -jar ...` | ▶️ |
| Thêm dependency SpringDoc | `pom.xml` | ✅ |
| Config Swagger bean | `OpenApiConfig.java` | ✅ |
| Thêm Swagger paths vào `permitAll()` | `SecurityConfig.java` | ✅ |
| Annotate AuthController (`@SecurityRequirements`) | ✅ |
| Annotate TaskController (11 endpoints) | ✅ |
| Annotate ProjectController (7 endpoints) | ✅ |
| Annotate UserController (6 endpoints) | ✅ |
| Test qua Swagger UI | Browser | ▶️ |
| Viết README | `README.md` | ✅ |
| Commit | Git | ▶️ |

---

## 💡 Ghi nhớ

```
@Tag(name="1. Auth")          → nhóm API, prefix số để sort đúng thứ tự
@Operation(summary="...")     → mô tả endpoint
@Parameter(description="...") → mô tả path/query param
@SecurityRequirements         → endpoint KHÔNG cần JWT
                                (chỉ dùng cho /auth/login & /auth/register)

application-dev.properties    → ddl-auto=update, show-sql=true
application-prod.properties   → ddl-auto=validate, show-sql=false,
                                credentials từ ${ENV_VAR}

CORS đã config sẵn trong SecurityConfig:
  localhost:5174 (Vue.js Vite)
  localhost:3000 (alternative)
```

---

**Người thực hiện:** Ngo Viet Hoang  
**Tuần:** 9/10  
**Next:** Tuần 10 — Demo + Review + Tổng kết
