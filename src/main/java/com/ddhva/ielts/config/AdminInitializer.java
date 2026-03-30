package com.ddhva.ielts.config;


import com.ddhva.ielts.enums.UserStatus;
import com.ddhva.ielts.model.Admin;
import com.ddhva.ielts.model.Role;
import com.ddhva.ielts.repositories.AdminRepository;
import com.ddhva.ielts.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void run(String @NonNull ... args) throws Exception {
        if(adminRepository.count() == 0 ){
            Role role = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setFullName("ADMIN");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(Objects.requireNonNull(passwordEncoder.encode("admin123")));
            admin.setDepartment("Manager");
            admin.setStatus(UserStatus.ACTIVE);
            admin.setAvatarUrl("/image/c21f969b5f03d33d43e04f8f136e7682.png");
            admin.setRole(role);
            adminRepository.save(admin);
        }
        System.out.println(">>> Saved admin");
    }
}
