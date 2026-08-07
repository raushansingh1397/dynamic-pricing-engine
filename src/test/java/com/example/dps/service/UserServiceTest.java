package com.example.dps.service;

import com.example.dps.dto.UserDTO;
import com.example.dps.entity.Role;
import com.example.dps.entity.Users;
import com.example.dps.repository.RoleRepository;
import com.example.dps.repository.UsersRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepo userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterUser_success() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setRoles(null);  // Will use default ROLE_USER

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepo.save(any(Users.class))).thenReturn(new Users());

        // Act
        String result = userService.registerUser(userDTO);

        // Assert
        assertEquals("User registered successfully!", result);
        verify(userRepo, times(1)).findByUsername("testuser");
        verify(roleRepo, times(1)).findByName("ROLE_USER");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepo, times(1)).save(any(Users.class));
    }

    @Test
    void testRegisterUser_usernameAlreadyTaken() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existinguser");
        userDTO.setPassword("password123");

        Users existingUser = new Users();
        existingUser.setUsername("existinguser");

        when(userRepo.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userDTO);
        });
        verify(userRepo, times(1)).findByUsername("existinguser");
        verify(userRepo, never()).save(any(Users.class));
    }

    @Test
    void testRegisterUser_defaultRoleNotFound() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("password123");
        userDTO.setRoles(null);  // Will use default ROLE_USER

        when(userRepo.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(userDTO);
        });
        verify(userRepo, times(1)).findByUsername("newuser");
        verify(roleRepo, times(1)).findByName("ROLE_USER");
        verify(userRepo, never()).save(any(Users.class));
    }

    @Test
    void testRegisterUser_withSpecificRoles() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("admin");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepo.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepo.save(any(Users.class))).thenReturn(new Users());

        // Act
        String result = userService.registerUser(userDTO);

        // Assert
        assertEquals("User registered successfully!", result);
        verify(userRepo, times(1)).findByUsername("admin");
        verify(roleRepo, times(2)).findByName(anyString());  // Called for each role
        verify(passwordEncoder, times(1)).encode("password123");
        
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(userCaptor.capture());
        assertEquals("admin", userCaptor.getValue().getUsername());
        assertEquals("encoded_password", userCaptor.getValue().getPassword());
        assertTrue(userCaptor.getValue().isEnabled());
        assertEquals(2, userCaptor.getValue().getRoles().size());
    }

    @Test
    void testRegisterUser_roleNotFound() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("ROLE_NONEXISTENT"));

        when(userRepo.findByUsername("user")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(userDTO);
        });
        verify(userRepo, times(1)).findByUsername("user");
        verify(roleRepo, times(1)).findByName("ROLE_NONEXISTENT");
        verify(userRepo, never()).save(any(Users.class));
    }

    @Test
    void testRegisterUser_passwordEncoded() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("plainpassword");
        userDTO.setRoles(null);

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plainpassword")).thenReturn("$2a$10$encodedpassword");
        when(userRepo.save(any(Users.class))).thenReturn(new Users());

        // Act
        userService.registerUser(userDTO);

        // Assert
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(userCaptor.capture());
        assertEquals("$2a$10$encodedpassword", userCaptor.getValue().getPassword());
    }
}

