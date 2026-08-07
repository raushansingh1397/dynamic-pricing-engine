package com.example.dps.service;

import com.example.dps.entity.Role;
import com.example.dps.entity.Users;
import com.example.dps.repository.UsersRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsersRepo usersRepo;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadUserByUsername_success() {
        // Arrange
        String username = "testuser";
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("password123");
        user.setEnabled(true);

        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        when(usersRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(usersRepo, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_userNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(usersRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
        verify(usersRepo, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_multipleRoles() {
        // Arrange
        String username = "admin";
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("adminpassword");
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        roles.add(userRole);
        roles.add(adminRole);
        user.setRoles(roles);

        when(usersRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_disabledUser() {
        // Arrange
        String username = "disableduser";
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("password123");
        user.setEnabled(false);

        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        when(usersRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertFalse(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testLoadUserByUsername_noRoles() {
        // Arrange
        String username = "noroleuser";
        Users user = new Users();
        user.setUsername(username);
        user.setPassword("password123");
        user.setEnabled(true);
        user.setRoles(new HashSet<>());  // No roles

        when(usersRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(0, userDetails.getAuthorities().size());
    }

    @Test
    void testLoadUserByUsername_exceptionMessage() {
        // Arrange
        String username = "missinguser";
        when(usersRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
        assertTrue(exception.getMessage().contains("User not found with username: " + username));
    }
}

