# ✅ **TUẦN 1: CÁC CẢI TIẾN QUAN TRỌNG (4 IMPROVEMENTS)**

## 📋 **OVERVIEW**

File **WEEK1_Domain_Analysis.md** đã được cập nhật với **4 improvements** quan trọng dựa trên feedback:

1. ✅ **Status Flow Validation** (TODO → IN_PROGRESS → DONE)
2. ✅ **Project Status Rules** (Không tạo Task khi COMPLETED/CANCELLED)
3. ✅ **equals & hashCode** (Cho Set<User> operations)
4. ✅ **Delete Strategy** (Soft Delete với audit trail)

---

## 🔹 **IMPROVEMENT #1: Status Flow Validation**

### **❌ TRƯỚC ĐÂY (Chỉ chặn DONE):**

```java
public void updateStatus(TaskStatus newStatus, User updatedBy) {
    if (this.status == TaskStatus.DONE) {
        throw new IllegalStateException("Không thể update Task đã DONE");
    }
    
    // Không kiểm tra flow → Có thể TODO → DONE (sai!)
    this.status = newStatus;
}
```

**Problem:**
- ❌ User có thể skip steps: TODO → DONE (không qua IN_PROGRESS)
- ❌ User có thể rollback: IN_PROGRESS → TODO
- ❌ Không theo workflow chuẩn

### **✅ SAU KHI CẢI TIẾN:**

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

**Allowed Transitions:**
```
TODO → IN_PROGRESS   ✅ OK
IN_PROGRESS → DONE   ✅ OK
TODO → DONE          ❌ FORBIDDEN (phải qua IN_PROGRESS)
DONE → *             ❌ FORBIDDEN (không cho revert)
IN_PROGRESS → TODO   ❌ FORBIDDEN (không cho lùi về)
```

**Benefits:**
- ✅ Workflow tuân theo quy trình
- ✅ Audit trail rõ ràng (biết task đã qua các steps nào)
- ✅ Tránh skip steps


---

## 🔹 **IMPROVEMENT #2: Project Status Rules**

### **❌ TRƯỚC ĐÂY (Không check Project status):**

```java
public Task(String title, String description, TaskPriority priority, 
            LocalDate deadline, Project project, User createdBy) {
    validateTitle(title);
    validateDeadline(deadline);
    validateProject(project);
    
    // Không kiểm tra project status → Có thể tạo task cho project COMPLETED (sai!)
    
    this.title = title;
    this.description = description;
    this.status = TaskStatus.TODO;
    // ...
}
```

**Problem:**
- ❌ Có thể tạo task cho project đã COMPLETED
- ❌ Có thể assign task cho project đã CANCELLED
- ❌ Không logic: project đã xong nhưng vẫn tạo task mới

### **✅ SAU KHI CẢI TIẾN:**

```java
public Task(String title, String description, TaskPriority priority, 
            LocalDate deadline, Project project, User createdBy) {
    validateTitle(title);
    validateDeadline(deadline);
    validateProject(project);
    validateProjectStatus(project);  // ← NEW RULE
    
    this.title = title;
    this.description = description;
    this.status = TaskStatus.TODO;
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
```

**Project Status Flow:**
```
PLANNING → ACTIVE    ✅ Bắt đầu dự án
ACTIVE → COMPLETED   ✅ Hoàn thành dự án
ACTIVE → CANCELLED   ✅ Hủy dự án
COMPLETED → *        ❌ Không thay đổi (final state)
CANCELLED → *        ❌ Không thay đổi (final state)
```

**Assignment Rule:**
```java
public void assignTo(User user) {
    // Rule 1: User phải là member của project
    if (!project.hasMember(user)) {
        throw new IllegalStateException(
            "User phải là member của Project trước khi assign Task"
        );
    }
    
    // Rule 2: Project phải ACTIVE (NEW!)
    if (project.getStatus() != ProjectStatus.ACTIVE) {
        throw new IllegalStateException(
            "Không thể assign Task khi Project " + project.getStatus()
        );
    }
    
    this.assignee = user;
}
```

**Benefits:**
- ✅ Tránh tạo tasks vô nghĩa cho project đã hoàn thành
- ✅ Consistency giữa Project và Task lifecycle


---

## 🔹 **IMPROVEMENT #3: equals & hashCode**

### **❌ TRƯỚC ĐÂY (Không override equals/hashCode):**

```java
public class User {
    private Long id;
    private String username;
    private Set<Role> roles;
    
    // Không có equals() & hashCode()
    // → Default Object.equals() so sánh memory address (sai!)
}
```

**Problem:**
```java
User user1 = new User("john", "pass", "john@email.com", "John");
user1.setId(1L);

User user2 = new User("john", "pass", "john@email.com", "John");
user2.setId(1L);

Set<User> members = new HashSet<>();
members.add(user1);
members.add(user2);

System.out.println(members.size());  // 2 (WRONG! Should be 1)
System.out.println(members.contains(user1));  // false (WRONG!)
```

**Why?**
- ❌ `Set<User> members` dùng `equals()` để check duplicate
- ❌ Default `equals()` so sánh memory address → 2 objects khác nhau dù có cùng ID
- ❌ `contains(user)` sẽ FAIL

### **✅ SAU KHI CẢI TIẾN:**

```java
public class User {
    private Long id;
    private String username;
    // ... other fields ...
    
    // ⚠️ CRITICAL: equals & hashCode for Set operations
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
}
```

**Test sau khi fix:**
```java
User user1 = new User("john", "pass", "john@email.com", "John");
user1.setId(1L);

User user2 = new User("john", "pass", "john@email.com", "John");
user2.setId(1L);

Set<User> members = new HashSet<>();
members.add(user1);
members.add(user2);

System.out.println(members.size());  // 1 ✅ CORRECT!
System.out.println(members.contains(user1));  // true ✅ CORRECT!
```

**Benefits:**
- ✅ Set operations hoạt động đúng
- ✅ `contains(user)` hoạt động đúng
- ✅ JPA entities hoạt động đúng với Collections

---

## 🔹 **IMPROVEMENT #4: Delete Strategy (Soft Delete)**

### **❌ TRƯỚC ĐÂY (Delete logic không rõ):**

```java
public void delete(User deletedBy) {
    if (!deletedBy.isManager()) {
        throw new SecurityException("Chỉ MANAGER mới được xóa Task");
    }
    
    // Logic delete... (không rõ: Hard delete hay Soft delete?)
}
```

**Problem:**
- ❌ Không rõ delete thật khỏi DB hay chỉ flag?
- ❌ Không biết ai chịu trách nhiệm xóa
- ❌ Không có audit trail
- ❌ Không thể restore nếu xóa nhầm

### **✅ SAU KHI CẢI TIẾN (Soft Delete):**

#### **1. Add fields to Task:**
```java
public class Task {
    private Long id;
    private String title;
    // ... other fields ...
    
    // Soft delete fields
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private User deletedBy;
    
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
    
    public boolean isActive() {
        return !this.deleted;
    }
}
```

#### **2. Database Schema:**
```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    -- ... other columns ...
    
    -- Soft delete fields
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    
    INDEX idx_deleted (deleted),
    FOREIGN KEY (deleted_by) REFERENCES users(id) ON DELETE SET NULL
);
```

#### **3. JPA Repository:**
```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find only active (not deleted) tasks
    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    List<Task> findAllActive();
    
    // Find active tasks by user
    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.deleted = false")
    List<Task> findActiveTasksByUser(@Param("userId") Long userId);
    
    // Find deleted tasks (for audit)
    @Query("SELECT t FROM Task t WHERE t.deleted = true")
    List<Task> findAllDeleted();
}
```

#### **4. Service Layer:**
```java
@Service
public class TaskService {
    
    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findByIdActive(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        task.delete(currentUser);
        taskRepository.save(task);
        
        log.info("Task {} soft deleted by user {}", taskId, currentUser.getUsername());
    }
    
    public void restoreTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        task.restore(currentUser);
        taskRepository.save(task);
        
        log.info("Task {} restored by user {}", taskId, currentUser.getUsername());
    }
}
```

**Benefits:**
- ✅ Giữ lại audit trail (ai xóa, khi nào xóa)
- ✅ Có thể restore nếu xóa nhầm
- ✅ Phân tích dữ liệu sau này (completed tasks)
- ✅ Maintain referential integrity (không phá FK)

**Cascade Delete:**
```java
public class Project {
    public void delete(User deletedBy) {
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


**Người thực hiện:** Ngo Viet Hoang  
