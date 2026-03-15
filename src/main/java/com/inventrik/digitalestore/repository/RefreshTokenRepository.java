package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    List<RefreshToken> findByUsername(String username);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.refreshToken = :refreshToken")
    void revokeByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.username = :username")
    void revokeAllByUsername(@Param("username") String username);
}
