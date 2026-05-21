package com.birmarket.service.impl;

import com.birmarket.dto.UserResponse;
import com.birmarket.enums.Role;
import com.birmarket.entity.User;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.UserMapper;
import com.birmarket.repository.UserRepository;
import com.birmarket.service.interfaces.UserService;
import com.birmarket.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponse> getAllUsers(Role role, Pageable pageable) {
        log.info("ActionLog.getAllUsers.start");

        Page<UserResponse> response;

        if (role != null) {
            response = userRepository.findByRole(role, pageable)
                    .map(userMapper::toResponse);
        } else {
            response = userRepository.findAll(pageable)
                    .map(userMapper::toResponse);
        }

        log.info("ActionLog.getAllUsers.end");
        return response;
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("ActionLog.getUserById.start");

        User user = findById(id);
        UserResponse response = userMapper.toResponse(user);

        log.info("ActionLog.getUserById.end");
        return response;
    }

    @Override
    public UserResponse blockUser(Long id) {
        log.info("ActionLog.blockUser.start");

        User user = findById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot block an admin account");
        }
        if (user.isBlocked()) {
            throw new BadRequestException("User is already blocked");
        }

        user.setBlocked(true);

        User saved = userRepository.save(user);
        UserResponse response = userMapper.toResponse(saved);

        log.info("ActionLog.blockUser.end");
        return response;
    }

    @Override
    public UserResponse unblockUser(Long id) {
        log.info("ActionLog.unblockUser.start");

        User user = findById(id);

        if (!user.isBlocked()) {
            throw new BadRequestException("User is not blocked");
        }

        user.setBlocked(false);

        User saved = userRepository.save(user);
        UserResponse response = userMapper.toResponse(saved);

        log.info("ActionLog.unblockUser.end");
        return response;
    }

    @Override
    public void deleteUser(Long id) {
        log.info("ActionLog.deleteUser.start");

        User user = findById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot delete an admin account");
        }

        user.setEnabled(false);
        user.setBlocked(true);
        userRepository.save(user);

        log.info("ActionLog.deleteUser.end");
    }

    @Override
    public UserResponse getMyProfile() {
        log.info("ActionLog.getMyProfile.start");

        UserResponse response = userMapper.toResponse(
                SecurityHelper.getCurrentUser()
        );

        log.info("ActionLog.getMyProfile.end");
        return response;
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}