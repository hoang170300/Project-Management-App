package taskmanagement.service;


import taskmanagement.dto.Request.LoginRequest;
import taskmanagement.dto.Request.RegisterRequest;
import taskmanagement.dto.Response.AuthResponse;

/**
 * Auth Service Interface
 * TUẦN 7: Authentication
 */
public interface AuthService {

    /**
     * Register new user
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login user
     */
    AuthResponse login(LoginRequest request);
}
