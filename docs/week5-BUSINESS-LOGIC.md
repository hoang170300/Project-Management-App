# TUẦN 5 - BUSINESS LOGIC (100% Complete)

## ✅ HOÀN THÀNH 100%

Folder này extends TUẦN 4 với **business logic validation**

---

## 🎯 New Features (TUẦN 5)

### 1. Exception Classes (3 files) ⭐ NEW
- `ResourceNotFoundException.java` - For 404 Not Found
- `BusinessRuleException.java` - For business rule violations
- `InvalidStatusTransitionException.java` - For status flow errors

### 2. Enhanced Services (2 files updated)
- `TaskServiceImpl.java` - With business rules
- `ProjectServiceImpl.java` - With validations

### 3. Business Rules Implemented

#### ✅ IMPROVEMENT #1: Status Flow Validation
```java
// TODO → IN_PROGRESS ✅
// IN_PROGRESS → DONE ✅
// TODO → DONE ❌ (must go through IN_PROGRESS)
// DONE → * ❌ (final state)
```

#### ✅ IMPROVEMENT #2: Project Status Rules
```java
// Can only add tasks to ACTIVE projects
// PLANNING/COMPLETED/CANCELLED projects cannot accept tasks
```

#### ✅ Additional Business Rules
- Assignee must be a valid user
- Project must exist when creating task
- Cannot delete task that is DONE (optional rule)
- Cannot update task that is deleted

---

## 📦 Files

### New Exception Classes
```
exception/
  ├── ResourceNotFoundException.java
  ├── BusinessRuleException.java
  └── InvalidStatusTransitionException.java
```

### Updated Services
```
service/impl/
  ├── TaskServiceImpl.java (enhanced)
  └── ProjectServiceImpl.java (enhanced)
```

**TOTAL: 5 files**

---

## 🚀 How to Use

### Option 1: Use Complete Week 5
```bash
cd /backend-v2/week-5-complete
mvn spring-boot:run
```

### Option 2: Apply to Week 4
```bash
cp -r /backend/src/main/java/com/taskmanagement/exception \
      /backend/src/main/java/com/taskmanagement/

# Replace service implementations
cp /backend/src/main/java/taskmanagement/service/impl/* \
   /backend/src/main/java/taskmanagement/service/impl/
```

---

## 🧪 Test Business Rules

### Test 1: Invalid Status Transition
```http
# Create task (status = TODO)
POST /api/tasks
{
  "title": "Test Task",
  "status": "TODO",
  ...
}

# Try to change TODO → DONE (should FAIL)
PUT /api/tasks/1/status
{
  "status": "DONE",
  "updatedBy": 1
}

# Expected: 400 Bad Request
{
  "error": "Cannot transition from TODO to DONE"
}
```

### Test 2: Assign to Non-member
```http
# Assign task to user not in project
PUT /api/tasks/1/assign/99?updatedBy=1

# Expected: 400 Bad Request
{
  "error": "User is not a member of this project"
}
```

### Test 3: Add Task to Non-Active Project
```http
# Try to add task to COMPLETED project
POST /api/tasks
{
  "projectId": 5, // COMPLETED project
  ...
}

# Expected: 400 Bad Request
{
  "error": "Cannot add tasks to non-ACTIVE project"
}
```

---

## 📋 Implementation Details

### InvalidStatusTransitionException.java
```java
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TaskStatus from, TaskStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
```

### Enhanced TaskServiceImpl
```java
@Override
public TaskResponseDTO updateTaskStatus(Long id, TaskStatus newStatus, Long updatedBy) {
    Task task = findTaskOrThrow(id);
    
    // IMPROVEMENT #1: Validate status transition
    if (!task.getStatus().canTransitionTo(newStatus)) {
        throw new InvalidStatusTransitionException(task.getStatus(), newStatus);
    }
    
    task.setStatus(newStatus);
    // ...
}
```

---

## ✅ Checklist

- [x] ResourceNotFoundException created
- [x] BusinessRuleException created
- [x] InvalidStatusTransitionException created
- [x] TaskServiceImpl enhanced with status validation
- [x] TaskServiceImpl enhanced with assignment validation
- [x] ProjectServiceImpl enhanced with status rules
- [x] All 4 improvements working
- [x] Tested with Postman

---

## 🎯 Next Steps

**TUẦN 6**: Global Exception Handler + ApiResponse wrapper
**TUẦN 7**: JWT Authentication
**Frontend**: Vue.js Kanban Board

---

**Status:** ✅ 100% COMPLETE
**4 Improvements:** ALL IMPLEMENTED ✅
