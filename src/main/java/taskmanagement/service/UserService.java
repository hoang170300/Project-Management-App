package taskmanagement.service;


import taskmanagement.dto.Request.UserRequest;
import taskmanagement.dto.Response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(UserRequest request);
    UserResponse findById(Long id);
    List<UserResponse> findAll();
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
    UserResponse restore(Long id);
}
