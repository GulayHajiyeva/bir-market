package com.birmarket.service;

import com.birmarket.dto.UserResponse;
import com.birmarket.entity.User;
import com.birmarket.enums.Role;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.UserMapper;
import com.birmarket.repository.UserRepository;
import com.birmarket.service.impl.UserServiceImpl;
import com.birmarket.util.SecurityHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityHelper> securityMock;

    private User customer;
    private User admin;

    @BeforeEach
    void setup() {

        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setFullName("Test Customer");
        customer.setRole(Role.CUSTOMER);
        customer.setBlocked(false);
        customer.setEnabled(true);

        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setFullName("Admin User");
        admin.setRole(Role.ADMIN);
        admin.setBlocked(false);
        admin.setEnabled(true);

        securityMock = mockStatic(SecurityHelper.class);
        securityMock.when(SecurityHelper::getCurrentUser)
                .thenReturn(customer);
    }

    @AfterEach
    void cleanup() {
        securityMock.close();
    }

    @Test
    void blockUser_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class)))
                .thenReturn(customer);

        UserResponse result = userService.blockUser(1L);

        assertNotNull(result);
        assertTrue(customer.isBlocked());

        verify(userRepository).save(customer);
    }

    @Test
    void blockUser_fails_for_admin() {

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                BadRequestException.class,
                () -> userService.blockUser(2L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void blockUser_fails_when_already_blocked() {

        customer.setBlocked(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                BadRequestException.class,
                () -> userService.blockUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void unblockUser_success() {

        customer.setBlocked(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class)))
                .thenReturn(customer);

        UserResponse result = userService.unblockUser(1L);

        assertNotNull(result);
        assertFalse(customer.isBlocked());

        verify(userRepository).save(customer);
    }

    @Test
    void unblockUser_fails_when_not_blocked() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                BadRequestException.class,
                () -> userService.unblockUser(1L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_disables_and_blocks_account() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(userRepository.save(any(User.class)))
                .thenReturn(customer);

        userService.deleteUser(1L);

        assertFalse(customer.isEnabled());
        assertTrue(customer.isBlocked());

        verify(userRepository).save(customer);
    }

    @Test
    void deleteUser_fails_for_admin() {

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                BadRequestException.class,
                () -> userService.deleteUser(2L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_returns_user() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("customer@test.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
    }

    @Test
    void getUserById_throws_when_not_found() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(99L)
        );
    }

    @Test
    void getMyProfile_returns_current_user() {

        UserResponse result = userService.getMyProfile();

        assertNotNull(result);
        assertEquals("customer@test.com", result.getEmail());
        assertEquals(Role.CUSTOMER, result.getRole());
    }
}