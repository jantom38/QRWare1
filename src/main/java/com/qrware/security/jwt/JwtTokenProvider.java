package com.qrware.security.jwt;

import com.qrware.domain.user.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String TOKEN_TYPE_KEY = "tokenType";

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        return generateTokenFromUser(userPrincipal, TokenType.ACCESS);
    }

    public String generateTokenFromUser(User user) {
        return generateTokenFromUser(user, TokenType.ACCESS);
    }

    public String generateRefreshToken(User user) {
        return generateTokenFromUser(user, TokenType.REFRESH);
    }

    private String generateTokenFromUser(User user, TokenType tokenType) {
        Date expiryDate = new Date(System.currentTimeMillis() + 
            (tokenType == TokenType.REFRESH ? refreshTokenExpirationInMs : jwtExpirationInMs));

        String authorities = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim(USER_ID_KEY, user.getId())
            .claim(USERNAME_KEY, user.getUsername())
            .claim(EMAIL_KEY, user.getEmail())
            .claim(FULL_NAME_KEY, user.getFullName())
            .claim(AUTHORITIES_KEY, authorities)
            .claim(TOKEN_TYPE_KEY, tokenType.name())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, getSecretKey())
            .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(USER_ID_KEY, Long.class);
    }

    public String getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(AUTHORITIES_KEY, String.class);
    }

    public TokenType getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get(TOKEN_TYPE_KEY, String.class);
        return TokenType.valueOf(tokenType);
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    public Date getIssuedAtDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }

    public boolean validateToken(String authToken) {
        try {
            getClaimsFromToken(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token) && getTokenTypeFromToken(token) == TokenType.REFRESH;
    }

    public long getRemainingValidityInSeconds(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long now = System.currentTimeMillis();
            return Math.max(0, (expiration.getTime() - now) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(getSecretKey())
            .parseClaimsJws(token)
            .getBody();
    }

    private byte[] getSecretKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            keyBytes = paddedKey;
        }
        return keyBytes;
    }

    public String refreshToken(String token, User user) {
        if (!canTokenBeRefreshed(token)) {
            throw new IllegalArgumentException("Token cannot be refreshed");
        }
        return generateTokenFromUser(user, TokenType.ACCESS);
    }

    public TokenInfo getTokenInfo(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return TokenInfo.builder()
                .username(claims.getSubject())
                .userId(claims.get(USER_ID_KEY, Long.class))
                .email(claims.get(EMAIL_KEY, String.class))
                .fullName(claims.get(FULL_NAME_KEY, String.class))
                .authorities(claims.get(AUTHORITIES_KEY, String.class))
                .tokenType(TokenType.valueOf(claims.get(TOKEN_TYPE_KEY, String.class)))
                .issuedAt(claims.getIssuedAt())
                .expiresAt(claims.getExpiration())
                .isExpired(isTokenExpired(token))
                .remainingValiditySeconds(getRemainingValidityInSeconds(token))
                .build();
        } catch (Exception e) {
            logger.error("Error extracting token info: {}", e.getMessage());
            return null;
        }
    }

    public enum TokenType {
        ACCESS, REFRESH
    }

    public static class TokenInfo {
        private String username;
        private Long userId;
        private String email;
        private String fullName;
        private String authorities;
        private TokenType tokenType;
        private Date issuedAt;
        private Date expiresAt;
        private boolean isExpired;
        private long remainingValiditySeconds;

        public static TokenInfoBuilder builder() {
            return new TokenInfoBuilder();
        }

        public static class TokenInfoBuilder {
            private String username;
            private Long userId;
            private String email;
            private String fullName;
            private String authorities;
            private TokenType tokenType;
            private Date issuedAt;
            private Date expiresAt;
            private boolean isExpired;
            private long remainingValiditySeconds;

            public TokenInfoBuilder username(String username) {
                this.username = username;
                return this;
            }

            public TokenInfoBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }

            public TokenInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public TokenInfoBuilder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public TokenInfoBuilder authorities(String authorities) {
                this.authorities = authorities;
                return this;
            }

            public TokenInfoBuilder tokenType(TokenType tokenType) {
                this.tokenType = tokenType;
                return this;
            }

            public TokenInfoBuilder issuedAt(Date issuedAt) {
                this.issuedAt = issuedAt;
                return this;
            }

            public TokenInfoBuilder expiresAt(Date expiresAt) {
                this.expiresAt = expiresAt;
                return this;
            }

            public TokenInfoBuilder isExpired(boolean isExpired) {
                this.isExpired = isExpired;
                return this;
            }

            public TokenInfoBuilder remainingValiditySeconds(long remainingValiditySeconds) {
                this.remainingValiditySeconds = remainingValiditySeconds;
                return this;
            }

            public TokenInfo build() {
                TokenInfo tokenInfo = new TokenInfo();
                tokenInfo.username = this.username;
                tokenInfo.userId = this.userId;
                tokenInfo.email = this.email;
                tokenInfo.fullName = this.fullName;
                tokenInfo.authorities = this.authorities;
                tokenInfo.tokenType = this.tokenType;
                tokenInfo.issuedAt = this.issuedAt;
                tokenInfo.expiresAt = this.expiresAt;
                tokenInfo.isExpired = this.isExpired;
                tokenInfo.remainingValiditySeconds = this.remainingValiditySeconds;
                return tokenInfo;
            }
        }

        public String getUsername() { return username; }
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getAuthorities() { return authorities; }
        public TokenType getTokenType() { return tokenType; }
        public Date getIssuedAt() { return issuedAt; }
        public Date getExpiresAt() { return expiresAt; }
        public boolean isExpired() { return isExpired; }
        public long getRemainingValiditySeconds() { return remainingValiditySeconds; }
    }
}