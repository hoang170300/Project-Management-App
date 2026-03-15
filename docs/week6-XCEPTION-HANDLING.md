# TUẦN 6 - EXCEPTION HANDLING (100% Complete)

## ✅ HOÀN THÀNH 100%

Global Exception Handler + ApiResponse wrapper cho clean API responses

---

## 🎯 New Features (TUẦN 6)

### 1. Response DTOs (2 files) ⭐ NEW
- `dto/Response/ApiResponse.java` - Generic wrapper for all API responses
- `dto/Response/ErrorResponse.java` - Error details

### 2. Global Exception Handler (1 file) ⭐ NEW
- `exception/GlobalExceptionHandler.java` - Centralized exception handling

### 3. Updated Controllers (2 files)
- `TaskController.java` - Return ApiResponse
- `ProjectController.java` - Return ApiResponse

---

## 📦 HTTP Status Codes

```
200 OK              - GET/PUT successful
201 Created         - POST successful
204 No Content      - DELETE successful
400 Bad Request     - Validation errors, InvalidStatusTransition
404 Not Found       - ResourceNotFoundException
409 Conflict        - BusinessRuleException
500 Internal Error  - Unexpected errors
```

---

## 🚀 Response Format

### Success Response
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": 1,
    "title": "Design homepage",
    ...
  },
  "timestamp": "2026-03-03T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Cannot transition task status from TODO to DONE",
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "details": "Task must go through IN_PROGRESS before DONE"
  },
  "timestamp": "2026-03-03T10:30:00"
}
```

---

## 📋 Exception Mappings

```java
@ExceptionHandler(ResourceNotFoundException.class)
→ 404 NOT FOUND

@ExceptionHandler(InvalidStatusTransitionException.class)
→ 400 BAD REQUEST

@ExceptionHandler(BusinessRuleException.class)
→ 409 CONFLICT

@ExceptionHandler(MethodArgumentNotValidException.class)
→ 400 BAD REQUEST (validation errors)

@ExceptionHandler(Exception.class)
→ 500 INTERNAL SERVER ERROR
```

---

**Status:** ✅ 100% COMPLETE
**Next:** TUẦN 7 - JWT Authentication
