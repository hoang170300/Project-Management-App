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
`http://localhost:8080/swagger-ui.html`

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