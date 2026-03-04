
USE TaskManagementDB;
GO

INSERT INTO users (username, password, email, full_name, deleted) VALUES
('john.manager', '123456', 'john@example.com', N'John Manager', 0),
('jane.admin', '123456', 'jane@example.com', N'Jane Admin', 0),
('bob.dev', '123456', 'bob@example.com', N'Bob Developer', 0),
('alice.qa', 'password123', 'alice@example.com', N'Alice QA', 0),
('charlie.dev', 'password123', 'charlie@example.com', N'Charlie Developer', 0),
('david.dev', 'password123', 'david@example.com', N'David Developer', 0),
('emma.qa', 'password123', 'emma@example.com', N'Emma QA', 0),
('frank.dev', 'password123', 'frank@example.com', N'Frank Developer', 0),
('grace.pm', 'password123', 'grace@example.com', N'Grace Project Manager', 0),
('henry.dev', 'password123', 'henry@example.com', N'Henry Developer', 0);

PRINT '✓ Inserted 10 users';

-- =============================================
-- INSERT USER_ROLES (10 records)
-- =============================================
INSERT INTO user_roles (user_id, role) VALUES
(1, 'MANAGER'),     -- john.manager
(2, 'MANAGER'),     -- jane.admin
(3, 'USER'),        -- bob.dev
(4, 'USER'),        -- alice.qa
(5, 'USER'),        -- charlie.dev
(6, 'USER'),        -- david.dev
(7, 'USER'),        -- emma.qa
(8, 'USER'),        -- frank.dev
(9, 'MANAGER'),     -- grace.pm
(10, 'USER');       -- henry.dev

PRINT '✓ Inserted 10 user roles';

INSERT INTO projects (name, description, status, start_date, end_date, created_by_id, deleted) VALUES
(N'Backend Development', 
 N'Spring Boot REST API with microservices architecture', 
 'ACTIVE', 
 '2026-01-01', 
 '2026-06-30', 
 1,  -- john.manager
 0),

(N'Security Implementation', 
 N'JWT authentication and role-based authorization', 
 'ACTIVE', 
 '2026-02-01', 
 '2026-04-30', 
 2,  -- jane.admin
 0),

(N'API Documentation', 
 N'Swagger documentation and user guides', 
 'ACTIVE', 
 '2026-02-15', 
 '2026-03-31', 
 2,  -- jane.admin
 0),

(N'Frontend Development', 
 N'React TypeScript application with Tailwind CSS', 
 'ACTIVE', 
 '2026-01-15', 
 '2026-07-31', 
 9,  -- grace.pm
 0),

(N'Database Migration', 
 N'Migrate legacy system to SQL Server', 
 'COMPLETED', 
 '2025-12-01', 
 '2026-01-31', 
 9,  -- grace.pm
 0);

PRINT '✓ Inserted 5 projects';

-- =============================================
-- INSERT PROJECT_MEMBERS (15 records)
-- Creator tự động là member (đã add ở application logic)
-- =============================================
INSERT INTO project_members (project_id, user_id) VALUES
-- Backend Development (project 1)
(1, 1),  -- john.manager (creator)
(1, 3),  -- bob.dev
(1, 5),  -- charlie.dev
(1, 6),  -- david.dev

-- Security Implementation (project 2)
(2, 2),  -- jane.admin (creator)
(2, 5),  -- charlie.dev
(2, 6),  -- david.dev
(2, 7),  -- emma.qa

-- API Documentation (project 3)
(3, 2),  -- jane.admin (creator)
(3, 8),  -- frank.dev

-- Frontend Development (project 4)
(4, 9),  -- grace.pm (creator)
(4, 10), -- henry.dev

-- Database Migration (project 5) 
(5, 9),  -- grace.pm (creator)
(5, 3),  -- bob.dev
(5, 4);  -- alice.qa

PRINT '✓ Inserted 15 project members';

-- =============================================
-- INSERT TASKS (35 tasks)
-- IMPROVEMENT #1: Status (TODO, IN_PROGRESS, DONE)
-- IMPROVEMENT #1: Priority (LOW, MEDIUM, HIGH, URGENT)
-- IMPROVEMENT #4: Soft delete (deleted = 0)
-- ĐÃ FIX: Thêm created_at nhỏ hơn deadline để vượt qua chk_task_deadline
-- =============================================

-- Project 1: Backend Development (15 tasks) -> created_at = '2026-01-01'
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_by_id, deadline, deleted, created_at) VALUES
(N'Setup Spring Boot Project', N'Initialize Spring Boot project with Maven', 'DONE', 'HIGH', 1, 3, 1, '2026-01-15', 0, '2026-01-01'),
(N'Configure SQL Server Connection', N'Setup database connection and properties', 'DONE', 'HIGH', 1, 3, 1, '2026-01-20', 0, '2026-01-01'),
(N'Create User Entity', N'JPA User entity with validation', 'DONE', 'MEDIUM', 1, 5, 1, '2026-01-25', 0, '2026-01-01'),
(N'Implement UserRepository', N'Spring Data JPA repository', 'DONE', 'MEDIUM', 1, 5, 1, '2026-01-28', 0, '2026-01-01'),
(N'Build UserService', N'Business logic for user management', 'IN_PROGRESS', 'MEDIUM', 1, 3, 1, '2026-02-10', 0, '2026-01-01'),
(N'Create UserController', N'REST API endpoints for users', 'IN_PROGRESS', 'MEDIUM', 1, 3, 1, '2026-02-15', 0, '2026-01-01'),
(N'Implement Task Entity', N'JPA Task entity', 'TODO', 'HIGH', 1, 6, 1, '2026-02-20', 0, '2026-01-01'),
(N'Build TaskRepository', N'Repository with custom queries', 'TODO', 'MEDIUM', 1, 6, 1, '2026-02-25', 0, '2026-01-01'),
(N'Create TaskService', N'Task business logic', 'TODO', 'HIGH', 1, NULL, 1, '2026-03-01', 0, '2026-01-01'),
(N'Build TaskController', N'Task REST APIs', 'TODO', 'MEDIUM', 1, NULL, 1, '2026-03-05', 0, '2026-01-01'),
(N'Implement Project Entity', N'Project entity mapping', 'TODO', 'MEDIUM', 1, NULL, 1, '2026-03-10', 0, '2026-01-01'),
(N'Global Exception Handler', N'Centralized error handling', 'TODO', 'LOW', 1, NULL, 1, '2026-03-15', 0, '2026-01-01'),
(N'Write Unit Tests', N'Service layer unit tests', 'TODO', 'MEDIUM', 1, 5, 1, '2026-03-20', 0, '2026-01-01'),
(N'Integration Tests', N'API integration tests', 'TODO', 'MEDIUM', 1, 5, 1, '2026-03-25', 0, '2026-01-01'),
(N'Performance Optimization', N'Optimize database queries', 'TODO', 'LOW', 1, NULL, 1, '2026-03-30', 0, '2026-01-01');

-- Project 2: Security Implementation (8 tasks) -> created_at = '2026-02-01'
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_by_id, deadline, deleted, created_at) VALUES
(N'Setup JWT Authentication', N'Implement JWT token generation', 'DONE', 'URGENT', 2, 5, 2, '2026-02-05', 0, '2026-02-01'),
(N'Implement BCrypt Password', N'Hash passwords with BCrypt', 'DONE', 'HIGH', 2, 5, 2, '2026-02-08', 0, '2026-02-01'),
(N'Role-Based Access Control', N'RBAC implementation', 'IN_PROGRESS', 'HIGH', 2, 6, 2, '2026-02-20', 0, '2026-02-01'),
(N'Spring Security Configuration', N'Configure Spring Security', 'IN_PROGRESS', 'MEDIUM', 2, 6, 2, '2026-02-25', 0, '2026-02-01'),
(N'Input Validation', N'Validate all user inputs', 'TODO', 'MEDIUM', 2, 7, 2, '2026-03-01', 0, '2026-02-01'),
(N'CORS Setup', N'Configure CORS policy', 'TODO', 'LOW', 2, NULL, 2, '2026-03-05', 0, '2026-02-01'),
(N'Security Testing', N'Penetration testing', 'TODO', 'HIGH', 2, 7, 2, '2026-03-10', 0, '2026-02-01'),
(N'Security Documentation', N'Document security measures', 'TODO', 'LOW', 2, NULL, 2, '2026-03-15', 0, '2026-02-01');

-- Project 3: API Documentation (5 tasks) -> created_at = '2026-02-01'
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_by_id, deadline, deleted, created_at) VALUES
(N'Setup Swagger', N'Integrate Swagger/OpenAPI', 'DONE', 'MEDIUM', 3, 8, 2, '2026-02-10', 0, '2026-02-01'),
(N'Document User APIs', N'User endpoint documentation', 'IN_PROGRESS', 'MEDIUM', 3, 8, 2, '2026-02-20', 0, '2026-02-01'),
(N'Document Task APIs', N'Task endpoint documentation', 'TODO', 'MEDIUM', 3, 8, 2, '2026-02-25', 0, '2026-02-01'),
(N'Document Project APIs', N'Project APIs documentation', 'TODO', 'LOW', 3, NULL, 2, '2026-03-01', 0, '2026-02-01'),
(N'Write API Usage Guide', N'API usage guide for clients', 'TODO', 'LOW', 3, NULL, 2, '2026-03-10', 0, '2026-02-01');

-- Project 4: Frontend Development (5 tasks) -> created_at = '2026-01-15'
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_by_id, deadline, deleted, created_at) VALUES
(N'Setup React Project', N'Initialize React with TypeScript', 'DONE', 'HIGH', 4, 10, 9, '2026-02-01', 0, '2026-01-15'),
(N'Build UI Components', N'Reusable React components', 'IN_PROGRESS', 'MEDIUM', 4, 10, 9, '2026-02-15', 0, '2026-01-15'),
(N'Integrate Backend APIs', N'Connect to Spring Boot backend', 'TODO', 'HIGH', 4, 10, 9, '2026-02-25', 0, '2026-01-15'),
(N'Authentication UI', N'Login/Logout pages', 'TODO', 'HIGH', 4, 10, 9, '2026-03-01', 0, '2026-01-15'),
(N'Deploy Frontend', N'Deploy to production', 'TODO', 'MEDIUM', 4, NULL, 9, '2026-03-15', 0, '2026-01-15');

-- Project 5: Database Migration (2 tasks - COMPLETED project) -> created_at = '2025-12-01'
INSERT INTO tasks (title, description, status, priority, project_id, assignee_id, created_by_id, deadline, deleted, created_at) VALUES
(N'Analyze Legacy Schema', N'Study old database structure', 'DONE', 'HIGH', 5, 3, 9, '2026-01-10', 0, '2025-12-01'),
(N'Migrate Data to SQL Server', N'ETL process to new DB', 'DONE', 'URGENT', 5, 3, 9, '2026-01-20', 0, '2025-12-01');

PRINT '✓ Inserted 35 tasks';
PRINT '';

-- =============================================
-- VERIFY DATA
-- =============================================
PRINT '========================================';
PRINT '   DATA VERIFICATION';
PRINT '========================================';

SELECT 'users' AS TableName, COUNT(*) AS RecordCount FROM users
UNION ALL
SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL
SELECT 'projects', COUNT(*) FROM projects
UNION ALL
SELECT 'project_members', COUNT(*) FROM project_members
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks;

PRINT '';

-- Statistics
PRINT 'Task Statistics by Status:';
SELECT 
    status,
    COUNT(*) AS count,
    CAST(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM tasks) AS DECIMAL(5,2)) AS percentage
FROM tasks
WHERE deleted = 0
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'TODO' THEN 1
        WHEN 'IN_PROGRESS' THEN 2
        WHEN 'DONE' THEN 3
    END;

PRINT '';
PRINT 'Task Statistics by Priority:';
SELECT 
    priority,
    COUNT(*) AS count
FROM tasks
WHERE deleted = 0
GROUP BY priority
ORDER BY 
    CASE priority
        WHEN 'URGENT' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        WHEN 'LOW' THEN 4
    END;

PRINT '';
PRINT '========================================';
PRINT '   ✓ SAMPLE DATA INSERTED!';
PRINT '========================================';
PRINT '';
PRINT 'Login credentials:';
PRINT '  username: john.manager / password: 123456 (MANAGER)';
PRINT '  username: jane.admin / password: 123456 (MANAGER)';
PRINT '  username: bob.dev / password: 123456 (USER)';
PRINT '';
PRINT 'Next: Run queries.sql to test';