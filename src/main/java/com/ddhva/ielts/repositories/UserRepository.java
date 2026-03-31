package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository <User, UUID>{
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUsername(String email, String username);
}
