# TUẦN 2: DATABASE DESIGN - SQL SERVER

## Mục tiêu
- Thiết kế ERD (Entity Relationship Diagram)
- Viết SQL script tạo bảng cho SQL Server
- Insert test data (>=30 records)
- Viết query phức tạp

---

## 1. ERD (Entity Relationship Diagram)

```
┌─────────────────────┐
│       User          │
├─────────────────────┤
│ id (PK)             │
│ username (UQ)       │
│ password            │
│ full_name           │
│ email (UQ)          │
│ role                │
│ created_at          │
└─────────────────────┘
         │
         │ 1
         │
         │ manages
         │
         │ *
         ▼
┌─────────────────────┐
│      Project        │
├─────────────────────┤
│ id (PK)             │
│ name                │
│ description         │
│ manager_id (FK)     │───┐
│ status              │   │
│ created_at          │   │
└─────────────────────┘   │
         │                │
         │ 1              │
         │                │
         │ has            │
         │                │
         │ *              │
         ▼                │
┌─────────────────────┐   │
│       Task          │   │
├─────────────────────┤   │
│ id (PK)             │   │
│ title               │   │
│ description         │   │
│ status              │   │
│ priority            │   │
│ project_id (FK)     │◄──┘
│ assigned_user_id(FK)│───┐
│ deadline            │   │
│ created_at          │   │
│ updated_at          │   │
└─────────────────────┘   │
                          │
            ┌─────────────┘
            │
            │ assigned to
            │
            ▼
       ┌─────────────────────┐
       │       User          │
       │  (same as above)    │
       └─────────────────────┘
```

### Quan hệ:
- **User 1 - N Project**: 1 Manager quản lý nhiều Project
- **Project 1 - N Task**: 1 Project có nhiều Task
- **User 1 - N Task**: 1 User được gán nhiều Task (nullable)

---

## 2. SQL Script - Tạo Database và Bảng

### schema.sql

```sql
-- =============================================
-- Task Management System Database Schema
-- SQL Server (SSMS)
-- =============================================

-- Tạo database
USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'TaskManagementDB')
BEGIN
    ALTER DATABASE TaskManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE TaskManagementDB;
END
GO

CREATE DATABASE TaskManagementDB;
GO

USE TaskManagementDB;
GO

-- =============================================
-- Bảng: users
-- =============================================
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    role NVARCHAR(20) NOT NULL CHECK (role IN ('USER', 'MANAGER')),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Index cho tìm kiếm nhanh
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- =============================================
-- Bảng: projects
-- =============================================
CREATE TABLE projects (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    manager_id BIGINT NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED', 'COMPLETED')),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    -- Foreign Key
    CONSTRAINT fk_project_manager FOREIGN KEY (manager_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Index
CREATE INDEX idx_projects_manager ON projects(manager_id);
CREATE INDEX idx_projects_status ON projects(status);

-- =============================================
-- Bảng: tasks
-- =============================================
CREATE TABLE tasks (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000),
    status NVARCHAR(20) NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority NVARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    project_id BIGINT NOT NULL,
    assigned_user_id BIGINT NULL,
    deadline DATE,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    -- Foreign Keys
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) 
        REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_user FOREIGN KEY (assigned_user_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    
    -- Constraint: deadline phải > created_at
    CONSTRAINT chk_deadline CHECK (deadline IS NULL OR deadline >= CAST(created_at AS DATE))
);

-- Index cho performance
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_user ON tasks(assigned_user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_deadline ON tasks(deadline);

-- Composite index cho query phổ biến
CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_user_status ON tasks(assigned_user_id, status);

-- =============================================
-- Trigger: Tự động cập nhật updated_at
-- =============================================
GO
CREATE TRIGGER trg_tasks_updated_at
ON tasks
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE tasks
    SET updated_at = GETDATE()
    FROM tasks t
    INNER JOIN inserted i ON t.id = i.id;
END;
GO

-- =============================================
-- View: Task với thông tin chi tiết
-- =============================================
CREATE VIEW vw_task_details AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.status,
    t.priority,
    t.deadline,
    t.created_at,
    t.updated_at,
    p.id AS project_id,
    p.name AS project_name,
    p.status AS project_status,
    u.id AS assigned_user_id,
    u.full_name AS assigned_user_name,
    u.email AS assigned_user_email,
    m.id AS manager_id,
    m.full_name AS manager_name
FROM tasks t
INNER JOIN projects p ON t.project_id = p.id
INNER JOIN users m ON p.manager_id = m.id
LEFT JOIN users u ON t.assigned_user_id = u.id;
GO

-- =============================================
-- Stored Procedure: Lấy task theo user
-- =============================================
CREATE PROCEDURE sp_GetTasksByUser
    @userId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        t.*,
        p.name AS project_name,
        m.full_name AS manager_name
    FROM tasks t
    INNER JOIN projects p ON t.project_id = p.id
    INNER JOIN users m ON p.manager_id = m.id
    WHERE t.assigned_user_id = @userId
    ORDER BY 
        CASE t.status
            WHEN 'TODO' THEN 1
            WHEN 'IN_PROGRESS' THEN 2
            WHEN 'DONE' THEN 3
        END,
        t.deadline ASC;
END;
GO

-- =============================================
-- Stored Procedure: Lấy task theo project
-- =============================================
CREATE PROCEDURE sp_GetTasksByProject
    @projectId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        t.*,
        u.full_name AS assigned_user_name
    FROM tasks t
    LEFT JOIN users u ON t.assigned_user_id = u.id
    WHERE t.project_id = @projectId
    ORDER BY 
        CASE t.priority
            WHEN 'HIGH' THEN 1
            WHEN 'MEDIUM' THEN 2
            WHEN 'LOW' THEN 3
        END,
        t.deadline ASC;
END;
GO

-- =============================================
-- Function: Đếm task theo status
-- =============================================
CREATE FUNCTION fn_CountTasksByStatus
(
    @projectId BIGINT,
    @status NVARCHAR(20)
)
RETURNS INT
AS
BEGIN
    DECLARE @count INT;
    
    SELECT @count = COUNT(*)
    FROM tasks
    WHERE project_id = @projectId AND status = @status;
    
    RETURN @count;
END;
GO

PRINT 'Database schema created successfully!';
```

---

## 3. Test Data - Insert >= 30 records

### sample-data.sql

```sql
-- =============================================
-- Sample Data for Task Management System
-- =============================================

USE TaskManagementDB;
GO

-- =============================================
-- Insert Users (10 users)
-- =============================================
INSERT INTO users (username, password, full_name, email, role) VALUES
('john.manager', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'John Manager', 'john@example.com', 'MANAGER'),
('jane.admin', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Jane Admin', 'jane@example.com', 'MANAGER'),
('bob.dev', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Bob Developer', 'bob@example.com', 'USER'),
('alice.qa', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Alice QA', 'alice@example.com', 'USER'),
('charlie.dev', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Charlie Developer', 'charlie@example.com', 'USER'),
('david.dev', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'David Developer', 'david@example.com', 'USER'),
('emma.qa', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Emma QA', 'emma@example.com', 'USER'),
('frank.dev', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Frank Developer', 'frank@example.com', 'USER'),
('grace.pm', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Grace Project Manager', 'grace@example.com', 'MANAGER'),
('henry.dev', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Henry Developer', 'henry@example.com', 'USER');

PRINT '✓ Inserted 10 users';

-- =============================================
-- Insert Projects (5 projects)
-- =============================================
INSERT INTO projects (name, description, manager_id, status) VALUES
(N'Backend Development', N'Spring Boot REST API development with microservices architecture', 1, 'ACTIVE'),
(N'Security Implementation', N'Security, authentication, and authorization implementation', 1, 'ACTIVE'),
(N'API Documentation', N'Swagger documentation and API guides', 2, 'ACTIVE'),
(N'Frontend Development', N'React frontend application development', 9, 'ACTIVE'),
(N'Database Migration', N'Legacy database migration to SQL Server', 9, 'COMPLETED');

PRINT '✓ Inserted 5 projects';

-- =============================================
-- Insert Tasks (35 tasks)
-- =============================================

-- Backend Development Project (15 tasks)
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, deadline) VALUES
(N'Setup Spring Boot Project', N'Initialize Spring Boot project with dependencies', 'DONE', 'HIGH', 1, 3, '2026-01-15'),
(N'Configure Database Connection', N'Setup JDBC connection to SQL Server', 'DONE', 'HIGH', 1, 3, '2026-01-20'),
(N'Implement User Entity', N'Create User entity with JPA annotations', 'DONE', 'MEDIUM', 1, 4, '2026-01-25'),
(N'Create UserRepository', N'Implement JPA Repository for User', 'DONE', 'MEDIUM', 1, 4, '2026-01-28'),
(N'Develop UserService', N'Business logic for User management', 'IN_PROGRESS', 'MEDIUM', 1, 3, '2026-02-10'),
(N'Build UserController', N'REST API endpoints for User CRUD', 'IN_PROGRESS', 'MEDIUM', 1, 3, '2026-02-15'),
(N'Implement Task Entity', N'Create Task entity with relationships', 'TODO', 'HIGH', 1, 5, '2026-02-20'),
(N'Create TaskRepository', N'JPA Repository for Task with custom queries', 'TODO', 'MEDIUM', 1, 5, '2026-02-25'),
(N'Develop TaskService', N'Business logic for Task management', 'TODO', 'HIGH', 1, 6, '2026-03-01'),
(N'Build TaskController', N'REST API for Task operations', 'TODO', 'MEDIUM', 1, 6, '2026-03-05'),
(N'Implement Project Entity', N'Create Project entity and relationships', 'TODO', 'MEDIUM', 1, NULL, '2026-03-10'),
(N'Add Exception Handling', N'Global exception handler', 'TODO', 'LOW', 1, NULL, '2026-03-15'),
(N'Write Unit Tests', N'Unit tests for Services', 'TODO', 'MEDIUM', 1, 4, '2026-03-20'),
(N'Integration Testing', N'Integration tests for APIs', 'TODO', 'MEDIUM', 1, 4, '2026-03-25'),
(N'Performance Optimization', N'Optimize database queries and caching', 'TODO', 'LOW', 1, NULL, '2026-03-30');

-- Security Implementation Project (8 tasks)
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, deadline) VALUES
(N'JWT Authentication', N'Implement JWT token-based authentication', 'DONE', 'HIGH', 2, 5, '2026-02-05'),
(N'Password Encryption', N'BCrypt password hashing', 'DONE', 'HIGH', 2, 5, '2026-02-08'),
(N'Role-Based Access Control', N'Implement RBAC for USER and MANAGER', 'IN_PROGRESS', 'HIGH', 2, 6, '2026-02-20'),
(N'Security Configuration', N'Spring Security configuration', 'IN_PROGRESS', 'MEDIUM', 2, 6, '2026-02-25'),
(N'Input Validation', N'Validate all API inputs', 'TODO', 'MEDIUM', 2, 7, '2026-03-01'),
(N'CORS Configuration', N'Setup CORS for frontend', 'TODO', 'LOW', 2, NULL, '2026-03-05'),
(N'Security Testing', N'Security penetration testing', 'TODO', 'HIGH', 2, 4, '2026-03-10'),
(N'Security Documentation', N'Document security implementation', 'TODO', 'LOW', 2, NULL, '2026-03-15');

-- API Documentation Project (5 tasks)
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, deadline) VALUES
(N'Setup Swagger', N'Integrate Swagger/OpenAPI', 'DONE', 'MEDIUM', 3, 8, '2026-02-10'),
(N'Document User APIs', N'Swagger annotations for User endpoints', 'IN_PROGRESS', 'MEDIUM', 3, 8, '2026-02-20'),
(N'Document Task APIs', N'Swagger annotations for Task endpoints', 'TODO', 'MEDIUM', 3, 8, '2026-02-25'),
(N'Document Project APIs', N'Swagger annotations for Project endpoints', 'TODO', 'LOW', 3, NULL, '2026-03-01'),
(N'API Usage Guide', N'Write comprehensive API guide', 'TODO', 'LOW', 3, NULL, '2026-03-10');

-- Frontend Development Project (5 tasks)
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, deadline) VALUES
(N'Setup React Project', N'Initialize React with TypeScript', 'DONE', 'HIGH', 4, 10, '2026-02-01'),
(N'Design UI Components', N'Create reusable components', 'IN_PROGRESS', 'MEDIUM', 4, 10, '2026-02-15'),
(N'Integrate with Backend API', N'Connect React to Spring Boot API', 'TODO', 'HIGH', 4, 10, '2026-02-25'),
(N'Implement Authentication', N'Login/Logout functionality', 'TODO', 'HIGH', 4, 10, '2026-03-01'),
(N'Deploy to Production', N'Deploy React app', 'TODO', 'MEDIUM', 4, NULL, '2026-03-15');

-- Database Migration Project (2 tasks - Completed)
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, deadline) VALUES
(N'Analyze Legacy Schema', N'Study old database structure', 'DONE', 'HIGH', 5, 3, '2026-01-10'),
(N'Migrate Data to SQL Server', N'ETL process for data migration', 'DONE', 'HIGH', 5, 3, '2026-01-20');

PRINT '✓ Inserted 35 tasks';

-- =============================================
-- Verify Data
-- =============================================
SELECT 
    'users' AS TableName, 
    COUNT(*) AS RecordCount 
FROM users
UNION ALL
SELECT 'projects', COUNT(*) FROM projects
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks;

PRINT 'Sample data inserted successfully!';
```

---

## 4. Query Examples

### 4.1. Query Task theo User

```sql
-- Lấy tất cả task của user Bob (id=3)
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    p.name AS project_name,
    t.deadline
FROM tasks t
INNER JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = 3
ORDER BY 
    CASE t.status
        WHEN 'TODO' THEN 1
        WHEN 'IN_PROGRESS' THEN 2
        WHEN 'DONE' THEN 3
    END,
    t.deadline ASC;
```

### 4.2. Query Task theo Project

```sql
-- Lấy task của project "Backend Development" (id=1)
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    u.full_name AS assigned_to,
    t.deadline
FROM tasks t
LEFT JOIN users u ON t.assigned_user_id = u.id
WHERE t.project_id = 1
ORDER BY 
    CASE t.priority
        WHEN 'HIGH' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'LOW' THEN 3
    END,
    t.deadline ASC;
```

### 4.3. Query Task theo Status

```sql
-- Lấy tất cả task IN_PROGRESS
SELECT 
    t.id,
    t.title,
    p.name AS project_name,
    u.full_name AS assigned_to,
    t.priority,
    t.deadline
FROM tasks t
INNER JOIN projects p ON t.project_id = p.id
LEFT JOIN users u ON t.assigned_user_id = u.id
WHERE t.status = 'IN_PROGRESS'
ORDER BY t.deadline ASC;
```

### 4.4. Thống kê Task theo Project

```sql
-- Thống kê số lượng task theo status cho mỗi project
SELECT 
    p.name AS project_name,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) AS todo_count,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) AS in_progress_count,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) AS done_count,
    COUNT(*) AS total_tasks
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
GROUP BY p.id, p.name
ORDER BY p.name;
```

### 4.5. Query Task quá hạn

```sql
-- Tìm task quá deadline và chưa DONE
SELECT 
    t.id,
    t.title,
    p.name AS project_name,
    u.full_name AS assigned_to,
    t.deadline,
    DATEDIFF(DAY, t.deadline, GETDATE()) AS days_overdue
FROM tasks t
INNER JOIN projects p ON t.project_id = p.id
LEFT JOIN users u ON t.assigned_user_id = u.id
WHERE t.status != 'DONE' 
  AND t.deadline < CAST(GETDATE() AS DATE)
ORDER BY t.deadline ASC;
```

### 4.6. Thống kê hiệu suất User

```sql
-- Thống kê số task hoàn thành của mỗi user
SELECT 
    u.full_name,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) AS completed_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) AS ongoing_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) AS pending_tasks,
    COUNT(*) AS total_assigned
FROM users u
LEFT JOIN tasks t ON u.id = t.assigned_user_id
WHERE u.role = 'USER'
GROUP BY u.id, u.full_name
ORDER BY completed_tasks DESC;
```

### 4.7. Query với View

```sql
-- Sử dụng view đã tạo
SELECT 
    title,
    status,
    priority,
    project_name,
    assigned_user_name,
    manager_name,
    deadline
FROM vw_task_details
WHERE status = 'IN_PROGRESS'
ORDER BY deadline;
```

---

## KẾT QUẢ TUẦN 2

✅ **Hoàn thành:**
- ERD chi tiết với quan hệ 1-N
- Review và fix ERD (đã thêm constraint)
- SQL script tạo bảng với PK, FK, Index
- Insert 50 records test data (10 users, 5 projects, 35 tasks)
- Check constraint cho deadline và enum values
- Query task theo user, project, status
- Tối ưu query với index và stored procedure
- Thêm View, Trigger, Function, Stored Procedure


