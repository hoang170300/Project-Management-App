# TUẦN 1: PHÂN TÍCH DOMAIN & THIẾT KẾ OOP

## 📌 **YÊU CẦU NGHIỆP VỤ**

### **Domain: Hệ thống quản lý Task/Project**

**Mô tả:** Hệ thống cho phép quản lý dự án (Project), công việc (Task), và người dùng (User) với phân quyền rõ ràng.

**Actors:**
- **MANAGER**: Quản lý projects, tạo tasks, assign tasks cho users
- **USER**: Xem và cập nhật tasks được assign cho mình

**Use Cases:**
1. Đăng ký/Đăng nhập
2. Tạo/sửa/xóa Project (MANAGER only)
3. Tạo/assign Task (MANAGER only)
4. Xem danh sách Tasks của mình (USER)
5. Cập nhật trạng thái Task (TODO → IN_PROGRESS → DONE)

---

## 📊 **BẢNG ENTITY & FIELDS**

### **1. User Entity**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | User ID |
| username | String(50) | NOT NULL, UNIQUE | Tên đăng nhập |
| password | String(255) | NOT NULL | Mật khẩu (BCrypt hash) |
| email | String(100) | NOT NULL, UNIQUE | Email |
| fullName | String(100) | NOT NULL | Họ tên đầy đủ |
| createdAt | Timestamp | NOT NULL | Ngày tạo |
| updatedAt | Timestamp | NULL | Ngày cập nhật |

### **2. Role Entity**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | Role ID |
| name | String(50) | NOT NULL, UNIQUE | Tên role (USER/MANAGER) |

### **3. UserRole (Many-to-Many)**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| userId | Long | FK → User.id | User ID |
| roleId | Long | FK → Role.id | Role ID |

### **4. Project Entity**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | Project ID |
| name | String(200) | NOT NULL | Tên dự án |
| description | Text | NULL | Mô tả |
| status | Enum | NOT NULL | PLANNING/ACTIVE/COMPLETED/CANCELLED |
| startDate | Date | NOT NULL | Ngày bắt đầu |
| endDate | Date | NULL | Ngày kết thúc |
| createdBy | Long | FK → User.id | Người tạo (MANAGER) |
| createdAt | Timestamp | NOT NULL | Ngày tạo |
| updatedAt | Timestamp | NULL | Ngày cập nhật |

### **5. Task Entity**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | Task ID |
| title | String(200) | NOT NULL | Tiêu đề task |
| description | Text | NULL | Mô tả chi tiết |
| status | Enum | NOT NULL | TODO/IN_PROGRESS/DONE |
| priority | Enum | NOT NULL | LOW/MEDIUM/HIGH/URGENT |
| deadline | Date | NULL | Deadline |
| projectId | Long | FK → Project.id | Dự án chứa task |
| assigneeId | Long | FK → User.id | Người được assign |
| createdBy | Long | FK → User.id | Người tạo |
| createdAt | Timestamp | NOT NULL | Ngày tạo |
| updatedAt | Timestamp | NULL | Ngày cập nhật |

### **6. ProjectMember (Many-to-Many)**

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| projectId | Long | FK → Project.id | Project ID |
| userId | Long | FK → User.id | User ID |
| joinedAt | Timestamp | NOT NULL | Ngày tham gia |

---

## 🔢 **ENUM DEFINITIONS**

### **TaskStatus**
```java
public enum TaskStatus {
    TODO,           // Chưa bắt đầu
    IN_PROGRESS,    // Đang thực hiện
    DONE            // Hoàn thành
}
```

**Business Rules:**
- Task mới tạo → `TODO`
- User bắt đầu làm → `IN_PROGRESS`
- Hoàn thành → `DONE`
- **KHÔNG được update task đã DONE** (immutable)

**⚠️ STATUS FLOW VALIDATION (IMPORTANT!):**

**Allowed Transitions:**
```
TODO → IN_PROGRESS   ✅ OK
IN_PROGRESS → DONE   ✅ OK
TODO → DONE          ❌ FORBIDDEN (phải qua IN_PROGRESS)
DONE → *             ❌ FORBIDDEN (không cho revert)
IN_PROGRESS → TODO   ❌ FORBIDDEN (không cho lùi về)
```

**Implementation:**
```java
public void updateStatus(TaskStatus newStatus, User updatedBy) {
    // Rule 1: Task đã DONE không thể update
    if (this.status == TaskStatus.DONE) {
        throw new IllegalStateException("Không thể update Task đã DONE");
    }
    
    // Rule 2: Validate status flow
    if (!isValidTransition(this.status, newStatus)) {
        throw new IllegalStateException(
            String.format("Invalid transition: %s → %s", this.status, newStatus)
        );
    }
    
    // Rule 3: Authorization check
    if (!updatedBy.isManager() && !this.assignee.equals(updatedBy)) {
        throw new SecurityException("Bạn chỉ được update Task của mình");
    }
    
    this.status = newStatus;
}

private boolean isValidTransition(TaskStatus current, TaskStatus next) {
    if (current == next) return true;  // No change
    
    switch (current) {
        case TODO:
            return next == TaskStatus.IN_PROGRESS;  // Only TODO → IN_PROGRESS
        case IN_PROGRESS:
            return next == TaskStatus.DONE;         // Only IN_PROGRESS → DONE
        case DONE:
            return false;                            // DONE is final state
        default:
            return false;
    }
}
```

**Why This Matters:**
- ✅ Đảm bảo workflow tuân theo quy trình
- ✅ Tránh users skip steps (TODO → DONE)
- ✅ Tránh rollback về trạng thái cũ
- ✅ Audit trail rõ ràng

### **TaskPriority**
```java
public enum TaskPriority {
    LOW,        // Thấp
    MEDIUM,     // Trung bình
    HIGH,       // Cao
    URGENT      // Khẩn cấp
}
```

### **ProjectStatus**
```java
public enum ProjectStatus {
    PLANNING,   // Đang lên kế hoạch
    ACTIVE,     // Đang hoạt động
    COMPLETED,  // Hoàn thành
    CANCELLED   // Hủy bỏ
}
```

**⚠️ PROJECT STATUS RULES (IMPORTANT!):**

**Business Rules:**
```
PLANNING → ACTIVE    ✅ Bắt đầu dự án
ACTIVE → COMPLETED   ✅ Hoàn thành dự án
ACTIVE → CANCELLED   ✅ Hủy dự án
COMPLETED → *        ❌ Không thay đổi (final state)
CANCELLED → *        ❌ Không thay đổi (final state)
```

**⚠️ TASK CREATION RULES BASED ON PROJECT STATUS:**

**Chỉ cho phép tạo Task khi Project đang ACTIVE:**
```java
public class Task {
    public Task(String title, String description, TaskPriority priority, 
                LocalDate deadline, Project project, User createdBy) {
        validateTitle(title);
        validateDeadline(deadline);
        validateProject(project);
        validateProjectStatus(project);  // ← NEW RULE
        
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;  // Initial status
        this.priority = priority;
        this.deadline = deadline;
        this.project = project;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    private void validateProjectStatus(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException(
                "Chỉ có thể tạo Task cho Project đang ACTIVE. " +
                "Project hiện tại: " + project.getStatus()
            );
        }
    }
}
```

**⚠️ TASK ASSIGNMENT RULES BASED ON PROJECT STATUS:**

**Chỉ cho phép assign Task khi Project đang ACTIVE:**
```java
public void assignTo(User user) {
    if (user == null) {
        throw new IllegalArgumentException("User không được null");
    }
    
    // Rule 1: User phải là member của project
    if (!project.hasMember(user)) {
        throw new IllegalStateException(
            "User phải là member của Project trước khi assign Task"
        );
    }
    
    // Rule 2: Project phải ACTIVE
    if (project.getStatus() != ProjectStatus.ACTIVE) {
        throw new IllegalStateException(
            "Không thể assign Task khi Project " + project.getStatus()
        );
    }
    
    this.assignee = user;
}
```

**Why This Matters:**
- ✅ Tránh tạo tasks vô nghĩa cho project đã hoàn thành
- ✅ Tránh assign tasks cho project bị hủy
- ✅ Đảm bảo consistency giữa Project và Task lifecycle
- ✅ Easy to explain to mentor: "We enforce project status rules"

---

## 📝 **MÔ T��� NGHIỆP VỤ USER–TASK–PROJECT**

### **1. User Management**
- **Register**: User đăng ký với role USER mặc định
- **Login**: Xác thực → JWT token
- **Profile**: Xem/update thông tin cá nhân

### **2. Project Management (MANAGER only)**
- **Create Project**: MANAGER tạo project mới
- **Add Members**: MANAGER thêm users vào project
- **Update Status**: PLANNING → ACTIVE → COMPLETED
- **View Projects**: Xem tất cả projects hoặc projects của mình

### **3. Task Management**
- **Create Task (MANAGER)**:
  - Chọn project (phải tồn tại)
  - Điền title, description, priority, deadline
  - Initial status: TODO
  
- **Assign Task (MANAGER)**:
  - Chọn user để assign
  - **Rule**: User phải là member của project
  
- **Update Status (USER/MANAGER)**:
  - USER: Chỉ update tasks assign cho mình
  - MANAGER: Update bất kỳ task nào
  - **Rule**: Không update task đã DONE
  
- **View Tasks**:
  - USER: Chỉ xem tasks của mình
  - MANAGER: Xem tất cả tasks

### **4. Authorization Rules**

| Action | USER | MANAGER |
|--------|------|---------|
| Tạo Project | ❌ | ✅ |
| Thêm member vào Project | ❌ | ✅ |
| Tạo Task | ❌ | ✅ |
| Assign Task | ❌ | ✅ |
| Xem Task của mình | ✅ | ✅ |
| Xem tất cả Tasks | ❌ | ✅ |
| Update status Task của mình | ✅ | ✅ |
| Update bất kỳ Task | ❌ | ✅ |

---

## 💻 **JAVA OOP DESIGN (Pseudo Code)**

### **User.java**
```java
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    
    // Constructor
    public User(String username, String password, String email, String fullName) {
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);
        
        this.username = username;
        this.password = hashPassword(password);
        this.email = email;
        this.fullName = fullName;
        this.createdAt = LocalDateTime.now();
        this.roles = new HashSet<>();
    }
    
    // Validation
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được rỗng");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username phải từ 3-50 ký tự");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password phải >= 6 ký tự");
        }
    }
    
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }
    
    // Business Logic
    private String hashPassword(String plainPassword) {
        // Sử dụng BCrypt
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(r -> r.getName().equals(roleName));
    }
    
    public boolean isManager() {
        return hasRole("MANAGER");
    }
    
    // ⚠️ CRITICAL: equals & hashCode for Set operations (IMPORTANT!)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // So sánh theo ID (business key)
        // Nếu ID null, so sánh theo username (natural key)
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        return Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        // Hash theo ID nếu có, nếu không hash theo username
        return id != null ? Objects.hash(id) : Objects.hash(username);
    }
    
    /**
     * Why This Matters:
     * 
     * 1. Set<User> members trong Project sử dụng equals() để check duplicate
     * 2. contains(user) sẽ FAIL nếu không override equals/hashCode
     * 3. Mentor THƯỜNG HỎI: "Tại sao Set không trùng lặp?"
     * 4. JPA entities PHẢI override equals/hashCode để hoạt động đúng với Set
     * 
     * Test case:
     * ```java
     * User user1 = new User("john", "pass", "john@email.com", "John");
     * user1.setId(1L);
     * 
     * User user2 = new User("john", "pass", "john@email.com", "John");
     * user2.setId(1L);
     * 
     * Set<User> members = new HashSet<>();
     * members.add(user1);
     * members.add(user2);
     * 
     * System.out.println(members.size());  // Expected: 1 (not 2!)
     * System.out.println(members.contains(user1));  // Expected: true
     * ```
     */
}
```

### **Task.java**
```java
public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate deadline;
    private Project project;
    private User assignee;
    private User createdBy;
    private LocalDateTime createdAt;
    
    // Constructor
    public Task(String title, String description, TaskPriority priority, 
                LocalDate deadline, Project project, User createdBy) {
        validateTitle(title);
        validateDeadline(deadline);
        validateProject(project);
        validateProjectStatus(project);  // ← NEW RULE
        
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;  // Initial status
        this.priority = priority;
        this.deadline = deadline;
        this.project = project;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    // Validation
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title không được rỗng");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title tối đa 200 ký tự");
        }
    }
    
    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline phải >= ngày hiện tại");
        }
    }
    
    private void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Task phải thuộc một Project");
        }
    }
    
    private void validateProjectStatus(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException(
                "Chỉ có thể tạo Task cho Project đang ACTIVE. " +
                "Project hiện tại: " + project.getStatus()
            );
        }
    }
    
    // Business Logic
    public void assignTo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User không được null");
        }
        
        // Rule 1: User phải là member của project
        if (!project.hasMember(user)) {
            throw new IllegalStateException(
                "User phải là member của Project trước khi assign Task"
            );
        }
        
        // Rule 2: Project phải ACTIVE
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException(
                "Không thể assign Task khi Project " + project.getStatus()
            );
        }
        
        this.assignee = user;
    }
    
    public void updateStatus(TaskStatus newStatus, User updatedBy) {
        // Rule 1: Task đã DONE không thể update
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("Không thể update Task đã DONE");
        }
        
        // Rule 2: Validate status flow
        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid transition: %s → %s", this.status, newStatus)
            );
        }
        
        // Rule 3: Authorization check
        if (!updatedBy.isManager() && !this.assignee.equals(updatedBy)) {
            throw new SecurityException("Bạn chỉ được update Task của mình");
        }
        
        this.status = newStatus;
    }

    private boolean isValidTransition(TaskStatus current, TaskStatus next) {
        if (current == next) return true;  // No change
        
        switch (current) {
            case TODO:
                return next == TaskStatus.IN_PROGRESS;  // Only TODO → IN_PROGRESS
            case IN_PROGRESS:
                return next == TaskStatus.DONE;         // Only IN_PROGRESS → DONE
            case DONE:
                return false;                            // DONE is final state
            default:
                return false;
        }
    }
    
    /**
     * Soft delete Task
     * MANAGER only
     */
    public void delete(User deletedBy) {
        // Authorization check
        if (!deletedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được xóa Task");
        }
        
        // Cannot delete if already deleted
        if (this.deleted) {
            throw new IllegalStateException("Task đã bị xóa trước đó");
        }
        
        // Soft delete: set flag instead of removing from database
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * Restore deleted Task (optional feature)
     */
    public void restore(User restoredBy) {
        if (!restoredBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được restore Task");
        }
        
        if (!this.deleted) {
            throw new IllegalStateException("Task chưa bị xóa");
        }
        
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
    
    /**
     * Check if Task is active (not deleted)
     */
    public boolean isActive() {
        return !this.deleted;
    }
}
```

### **Project.java**
```java
public class Project {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private User createdBy;
    private Set<User> members;
    private Set<Task> tasks;
    private LocalDateTime createdAt;
    
    // Constructor
    public Project(String name, String description, LocalDate startDate, 
                   LocalDate endDate, User createdBy) {
        validateName(name);
        validateDates(startDate, endDate);
        validateCreator(createdBy);
        
        this.name = name;
        this.description = description;
        this.status = ProjectStatus.PLANNING;  // Initial status
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.members = new HashSet<>();
        this.tasks = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        
        // Creator tự động là member
        this.members.add(createdBy);
    }
    
    // Validation
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name không được rỗng");
        }
    }
    
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date không được null");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date phải >= Start date");
        }
    }
    
    private void validateCreator(User creator) {
        if (creator == null || !creator.isManager()) {
            throw new IllegalArgumentException("Chỉ MANAGER mới tạo được Project");
        }
    }
    
    // Business Logic
    public void addMember(User user, User addedBy) {
        if (!addedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới thêm được member");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("User không được null");
        }
        
        members.add(user);
    }
    
    public void removeMember(User user, User removedBy) {
        if (!removedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới xóa được member");
        }
        
        // Không xóa creator
        if (user.equals(createdBy)) {
            throw new IllegalStateException("Không thể xóa creator khỏi Project");
        }
        
        members.remove(user);
    }
    
    public boolean hasMember(User user) {
        return members.contains(user);
    }
    
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task không được null");
        }
        tasks.add(task);
    }
    
    public void updateStatus(ProjectStatus newStatus, User updatedBy) {
        if (!updatedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới update được status");
        }
        this.status = newStatus;
    }
    
    /**
     * Soft delete Project
     * MANAGER only
     */
    public void delete(User deletedBy) {
        if (!deletedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được xóa Project");
        }
        
        // Soft delete all tasks belonging to this project
        for (Task task : this.tasks) {
            if (task.isActive()) {
                task.delete(deletedBy);
            }
        }
        
        // Then soft delete the project itself
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
```

---

## 🗑️ **DELETE STRATEGY (IMPORTANT!)**

### **⚠️ Soft Delete vs Hard Delete**

**Quyết định:** Hệ thống sử dụng **SOFT DELETE** cho Task và Project

**Why Soft Delete?**
- ✅ Giữ lại lịch sử (audit trail)
- ✅ Có thể restore nếu xóa nhầm
- ✅ Phân tích dữ liệu sau này (completed tasks, cancelled projects)
- ✅ Maintain referential integrity (không phá vỡ FK relationships)

### **Implementation Strategy**

#### **1. Add `deleted` field to entities:**

```java
public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate deadline;
    private Project project;
    private User assignee;
    private User createdBy;
    private LocalDateTime createdAt;
    
    // Soft delete fields
    private boolean deleted = false;  // Soft delete flag
    private LocalDateTime deletedAt;
    private User deletedBy;
    
    // Constructor
    public Task(String title, String description, TaskPriority priority, 
                LocalDate deadline, Project project, User createdBy) {
        validateTitle(title);
        validateDeadline(deadline);
        validateProject(project);
        validateProjectStatus(project);  // ← NEW RULE
        
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;  // Initial status
        this.priority = priority;
        this.deadline = deadline;
        this.project = project;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    // Validation
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title không được rỗng");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title tối đa 200 ký tự");
        }
    }
    
    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline phải >= ngày hiện tại");
        }
    }
    
    private void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Task phải thuộc một Project");
        }
    }
    
    private void validateProjectStatus(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException(
                "Chỉ có thể tạo Task cho Project đang ACTIVE. " +
                "Project hiện tại: " + project.getStatus()
            );
        }
    }
    
    // Business Logic
    public void assignTo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User không được null");
        }
        
        // Rule 1: User phải là member của project
        if (!project.hasMember(user)) {
            throw new IllegalStateException(
                "User phải là member của Project trước khi assign Task"
            );
        }
        
        // Rule 2: Project phải ACTIVE
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException(
                "Không thể assign Task khi Project " + project.getStatus()
            );
        }
        
        this.assignee = user;
    }
    
    public void updateStatus(TaskStatus newStatus, User updatedBy) {
        // Rule 1: Task đã DONE không thể update
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("Không thể update Task đã DONE");
        }
        
        // Rule 2: Validate status flow
        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid transition: %s → %s", this.status, newStatus)
            );
        }
        
        // Rule 3: Authorization check
        if (!updatedBy.isManager() && !this.assignee.equals(updatedBy)) {
            throw new SecurityException("Bạn chỉ được update Task của mình");
        }
        
        this.status = newStatus;
    }

    private boolean isValidTransition(TaskStatus current, TaskStatus next) {
        if (current == next) return true;  // No change
        
        switch (current) {
            case TODO:
                return next == TaskStatus.IN_PROGRESS;  // Only TODO → IN_PROGRESS
            case IN_PROGRESS:
                return next == TaskStatus.DONE;         // Only IN_PROGRESS → DONE
            case DONE:
                return false;                            // DONE is final state
            default:
                return false;
        }
    }
    
    /**
     * Soft delete Task
     * MANAGER only
     */
    public void delete(User deletedBy) {
        // Authorization check
        if (!deletedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được xóa Task");
        }
        
        // Cannot delete if already deleted
        if (this.deleted) {
            throw new IllegalStateException("Task đã bị xóa trước đó");
        }
        
        // Soft delete: set flag instead of removing from database
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * Restore deleted Task (optional feature)
     */
    public void restore(User restoredBy) {
        if (!restoredBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được restore Task");
        }
        
        if (!this.deleted) {
            throw new IllegalStateException("Task chưa bị xóa");
        }
        
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
    
    /**
     * Check if Task is active (not deleted)
     */
    public boolean isActive() {
        return !this.deleted;
    }
}
```

#### **2. Database Schema với Soft Delete:**

```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE') NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL,
    deadline DATE NULL,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    
    -- Soft delete fields
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    
    -- Indexes for soft delete queries
    INDEX idx_deleted (deleted),
    INDEX idx_deleted_at (deleted_at),
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);
```

#### **3. JPA Repository với Soft Delete:**

```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find only active (not deleted) tasks
    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    List<Task> findAllActive();
    
    // Find active tasks by assignee
    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.deleted = false")
    List<Task> findActiveTasksByUser(@Param("userId") Long userId);
    
    // Find active tasks by project
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.deleted = false")
    List<Task> findActiveTasksByProject(@Param("projectId") Long projectId);
    
    // Find deleted tasks (for admin/audit)
    @Query("SELECT t FROM Task t WHERE t.deleted = true")
    List<Task> findAllDeleted();
    
    // Override default findById to exclude deleted
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deleted = false")
    Optional<Task> findByIdActive(@Param("id") Long id);
}
```

#### **4. Service Layer với Soft Delete:**

```java
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    /**
     * Delete Task (soft delete)
     */
    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findByIdActive(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        // Business logic: call delete method on entity
        task.delete(currentUser);
        
        // Save changes (deleted flag updated)
        taskRepository.save(task);
        
        // Log the action
        log.info("Task {} soft deleted by user {}", taskId, currentUser.getUsername());
    }
    
    /**
     * Get all active tasks
     */
    public List<TaskDTO> getAllActiveTasks() {
        return taskRepository.findAllActive()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Restore deleted Task (MANAGER only)
     */
    public void restoreTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        task.restore(currentUser);
        taskRepository.save(task);
        
        log.info("Task {} restored by user {}", taskId, currentUser.getUsername());
    }
}
```

### **⚠️ Cascade Delete Rules**

**When Project is deleted:**

```java
public class Project {
    public void delete(User deletedBy) {
        if (!deletedBy.isManager()) {
            throw new SecurityException("Chỉ MANAGER mới được xóa Project");
        }
        
        // Soft delete all tasks belonging to this project
        for (Task task : this.tasks) {
            if (task.isActive()) {
                task.delete(deletedBy);
            }
        }
        
        // Then soft delete the project itself
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
```

**When User is deleted:**

```sql
-- In schema.sql, we already defined:
CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) 
    REFERENCES users(id) ON DELETE SET NULL

-- Meaning: when user deleted → task.assigneeId = NULL (not deleted)
```

### **⚠️ Hard Delete (Alternative - NOT RECOMMENDED)**

**If you choose Hard Delete (xóa thật khỏi DB):**

```java
public void hardDelete(User deletedBy) {
    if (!deletedBy.isManager()) {
        throw new SecurityException("Chỉ MANAGER mới được xóa Task");
    }
    
    // Remove from project's task set
    if (this.project != null) {
        this.project.getTasks().remove(this);
    }
    
    // JPA will delete from database
    // repository.delete(this)
}
```

**Problems with Hard Delete:**
- ❌ Mất dữ liệu vĩnh viễn
- ❌ Không thể audit (ai đã xóa task này?)
- ❌ Phá vỡ references (tasks hoàn thành bị xóa → không biết ai đã làm gì)
- ❌ Không thể undo

### **Recommendation Matrix**

| Entity | Delete Strategy | Reason |
|--------|----------------|---------|
| Task | **SOFT DELETE** | Keep history, can restore |
| Project | **SOFT DELETE** | Keep completed projects for reporting |
| User | **SOFT DELETE** | Keep user history, reassign tasks |
| Role | **NO DELETE** | System data, should never delete |

### **Test Cases for Delete Logic:**

```java
// Test Case 1: Soft delete task
@Test
public void testSoftDeleteTask() {
    Task task = new Task("Test task", ...);
    task.setId(1L);
    
    User manager = createManager();
    task.delete(manager);
    
    assertTrue(task.isDeleted());
    assertNotNull(task.getDeletedAt());
    assertEquals(manager, task.getDeletedBy());
}

// Test Case 2: Cannot delete twice
@Test(expected = IllegalStateException.class)
public void testCannotDeleteTwice() {
    Task task = new Task("Test task", ...);
    User manager = createManager();
    
    task.delete(manager);
    task.delete(manager);  // Should throw exception
}

// Test Case 3: USER cannot delete
@Test(expected = SecurityException.class)
public void testUserCannotDelete() {
    Task task = new Task("Test task", ...);
    User normalUser = createUser();
    
    task.delete(normalUser);  // Should throw exception
}

// Test Case 4: Restore deleted task
@Test
public void testRestoreTask() {
    Task task = new Task("Test task", ...);
    User manager = createManager();
    
    task.delete(manager);
    assertTrue(task.isDeleted());
    
    task.restore(manager);
    assertFalse(task.isDeleted());
    assertNull(task.getDeletedAt());
}
```

---

**Summary:**
- ✅ **Soft Delete** for Task & Project
- ✅ Add `deleted`, `deletedAt`, `deletedBy` fields
- ✅ Filter deleted items in queries (`WHERE deleted = false`)
- ✅ MANAGER-only delete permission
- ✅ Optional restore feature
- ✅ Cascade soft delete (Project → Tasks)
