package com.example.dps.service;

import com.example.dps.dto.UserDTO;
import com.example.dps.entity.Role;
import com.example.dps.entity.Users;
import com.example.dps.repository.RoleRepository;
import com.example.dps.repository.UsersRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private final UsersRepo userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsersRepo userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(UserDTO userDTO){
        if(userRepo.findByUsername(userDTO.getUsername()).isPresent()){
            throw new IllegalArgumentException("Username is already taken!");
        }
        Users user = new Users();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();

        if(userDTO.getRoles()== null|| userDTO.getRoles().isEmpty()){
            Role userRole = roleRepo.findByName("ROLE_USER").orElseThrow(()->new RuntimeException("Default ROLE_USER not found in DB. Seed roles table first."));
            roles.add(userRole);
        } else {
            for(String roleName: userDTO.getRoles()){
                Role role = roleRepo.findByName(roleName)
                        .orElseThrow(()->new RuntimeException("Role "+ roleName + " does not exist."));
                roles.add(role);
            }
        }
        user.setRoles(roles);
        userRepo.save(user);
        return "User registered successfully!";
    }
}
