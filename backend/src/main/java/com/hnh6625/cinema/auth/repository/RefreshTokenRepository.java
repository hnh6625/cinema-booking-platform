package com.hnh6625.cinema.auth.repository;

import com.hnh6625.cinema.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
