package com.ddhva.ielts.config;

import com.ddhva.ielts.model.Role;
import com.ddhva.ielts.model.User;
import com.ddhva.ielts.repositories.RoleRepository;
import com.ddhva.ielts.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Role not found. ID: " + user.getRole().getId()));
        return org.springframework.security.core.userdetails.User.builder()
                .username(getUserByEmailOrUsername(user.getEmail(), user.getUsername()).getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role.getName()))
                .build();
    }


    private User getUserByEmailOrUsername(String email, String username){
        return userRepository.findByEmailOrUsername(email, username)
                .orElseThrow(() -> new RuntimeException("User not found with username or email : " + username));
    }
}
