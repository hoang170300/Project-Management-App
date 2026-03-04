
CREATE DATABASE TaskManagementDB;
GO

USE TaskManagementDB;
GO

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    full_name NVARCHAR(100) NOT NULL,
    
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME2 NULL,
    
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted ON users(deleted);

PRINT '✓ Table users created with soft delete';


CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role NVARCHAR(50) NOT NULL CHECK (role IN ('USER', 'MANAGER')),
    
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

PRINT '✓ Table user_roles created';

CREATE TABLE projects (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    
    status NVARCHAR(20) NOT NULL DEFAULT 'PLANNING' 
        CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    
    start_date DATE NOT NULL,
    end_date DATE NULL,
    
    created_by_id BIGINT NOT NULL,
   
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME2 NULL,
    deleted_by_id BIGINT NULL,
    
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT fk_project_created_by FOREIGN KEY (created_by_id) 
        REFERENCES users(id),
    CONSTRAINT fk_project_deleted_by FOREIGN KEY (deleted_by_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_project_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_projects_created_by ON projects(created_by_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_deleted ON projects(deleted);

PRINT '✓ Table projects created with status flow and soft delete';

CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) 
        REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);

PRINT '✓ Table project_members created';

CREATE TABLE tasks (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000),
    
    status NVARCHAR(20) NOT NULL DEFAULT 'TODO' 
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
   
    priority NVARCHAR(20) NOT NULL DEFAULT 'MEDIUM' 
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    
    project_id BIGINT NOT NULL,
    assignee_id BIGINT NULL,
    created_by_id BIGINT NOT NULL,
    
    deadline DATE NULL,
    
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME2 NULL,
    deleted_by_id BIGINT NULL,
    
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) 
        REFERENCES projects(id) ON DELETE CASCADE,
     
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) 
        REFERENCES users(id) ON DELETE NO ACTION,
        
    CONSTRAINT fk_task_created_by FOREIGN KEY (created_by_id) 
        REFERENCES users(id) ON DELETE NO ACTION,
        
    CONSTRAINT fk_task_deleted_by FOREIGN KEY (deleted_by_id) 
        REFERENCES users(id) ON DELETE NO ACTION,
    
    CONSTRAINT chk_task_deadline CHECK (deadline IS NULL OR deadline >= CAST(created_at AS DATE))
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_deadline ON tasks(deadline);
CREATE INDEX idx_tasks_deleted ON tasks(deleted);

CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_assignee_status ON tasks(assignee_id, status);
CREATE INDEX idx_tasks_deleted_status ON tasks(deleted, status);

PRINT '✓ Table tasks created with all improvements and fixed constraints';
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

PRINT '✓ Trigger trg_tasks_updated_at created';

GO
CREATE VIEW vw_task_details AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.status,
    t.priority,
    t.deadline,
    t.deleted,
    t.created_at,
    t.updated_at,
    
    -- Project info
    p.id AS project_id,
    p.name AS project_name,
    p.status AS project_status,
    
    -- Assignee info
    a.id AS assignee_id,
    a.full_name AS assignee_name,
    a.email AS assignee_email,
    
    -- Creator info
    c.id AS creator_id,
    c.full_name AS creator_name,
    
    -- Project creator (manager)
    m.id AS manager_id,
    m.full_name AS manager_name
    
FROM tasks t
INNER JOIN projects p ON t.project_id = p.id
INNER JOIN users c ON t.created_by_id = c.id
INNER JOIN users m ON p.created_by_id = m.id
LEFT JOIN users a ON t.assignee_id = a.id;
GO

PRINT '✓ View vw_task_details created';

GO
CREATE PROCEDURE sp_GetActiveTasksByUser
    @userId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        t.*,
        p.name AS project_name,
        p.status AS project_status
    FROM tasks t
    INNER JOIN projects p ON t.project_id = p.id
    WHERE t.assignee_id = @userId 
      AND t.deleted = 0  
      AND p.deleted = 0 
    ORDER BY 
        CASE t.status
            WHEN 'TODO' THEN 1
            WHEN 'IN_PROGRESS' THEN 2
            WHEN 'DONE' THEN 3
        END,
        t.deadline ASC;
END;
GO

PRINT '✓ Stored procedure sp_GetActiveTasksByUser created';

GO
CREATE PROCEDURE sp_GetActiveTasksByProject
    @projectId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        t.*,
        a.full_name AS assignee_name
    FROM tasks t
    LEFT JOIN users a ON t.assignee_id = a.id
    WHERE t.project_id = @projectId 
      AND t.deleted = 0  
    ORDER BY 
        CASE t.priority
            WHEN 'URGENT' THEN 1
            WHEN 'HIGH' THEN 2
            WHEN 'MEDIUM' THEN 3
            WHEN 'LOW' THEN 4
        END,
        t.deadline ASC;
END;
GO

PRINT '✓ Stored procedure sp_GetActiveTasksByProject created';

GO
CREATE FUNCTION fn_CountActiveTasksByStatus
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
    WHERE project_id = @projectId 
      AND status = @status
      AND deleted = 0; 
    
    RETURN @count;
END;
GO

PRINT '✓ Function fn_CountActiveTasksByStatus created';

SELECT 
    TABLE_NAME,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = t.TABLE_NAME) AS ColumnCount
FROM INFORMATION_SCHEMA.TABLES t
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

