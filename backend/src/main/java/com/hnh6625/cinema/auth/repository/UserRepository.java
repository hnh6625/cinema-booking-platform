package com.hnh6625.cinema.auth.repository;

import com.hnh6625.cinema.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
}
