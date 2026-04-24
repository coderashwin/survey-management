package com.sbi.branchdarpan.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.sbi.branchdarpan.config.JwtProperties;
import com.sbi.branchdarpan.model.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(UserPrincipal user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.expiration());

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("pfid", user.getPfid())
            .claim("name", user.getDisplayName())
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .claim("circleCode", user.getCircleCode())
            .claim("aoCode", user.getAoCode())
            .claim("rboCode", user.getRboCode())
            .claim("branchCode", user.getBranchCode())
            .claim("branchName", user.getBranchName())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public UserPrincipal getUserFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return UserPrincipal.builder()
            .id(claims.get("userId", Long.class))
            .pfid(claims.get("pfid", String.class))
            .displayName(claims.get("name", String.class))
            .role(Role.valueOf(claims.get("role", String.class)))
            .circleCode(claims.get("circleCode", String.class))
            .aoCode(claims.get("aoCode", String.class))
            .rboCode(claims.get("rboCode", String.class))
            .branchCode(claims.get("branchCode", String.class))
            .branchName(claims.get("branchName", String.class))
            .build();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
