package com.birmarket.service.interfaces;

import com.birmarket.dto.UserResponse;
import com.birmarket.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Role role, Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse blockUser(Long id);
    UserResponse unblockUser(Long id);
    void deleteUser(Long id);
    UserResponse getMyProfile();
}
