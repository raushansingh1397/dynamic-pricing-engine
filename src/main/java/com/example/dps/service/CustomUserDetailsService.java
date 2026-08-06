package com.example.dps.service;

import com.example.dps.entity.Users;
import com.example.dps.repository.UsersRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private  final UsersRepo repo;

    public CustomUserDetailsService(UsersRepo repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = repo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found with username: "+username));
        List<SimpleGrantedAuthority> authorityList = user.getRoles().stream().
                map(role->new SimpleGrantedAuthority(role.getName())).toList();

        return new User(user.getUsername(),user.getPassword(),
                user.isEnabled(),true,true,
                true,authorityList);

    }
}
