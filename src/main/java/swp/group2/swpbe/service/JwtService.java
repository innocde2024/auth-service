package swp.group2.swpbe.service;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import swp.group2.swpbe.constant.UserRole;
import swp.group2.swpbe.entities.User;
import swp.group2.swpbe.exception.ApiRequestException;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String JWT_SECRET;
    @Value("${jwt.refresh.secret}")
    private String JWT_REFRESH_SECRET;

    private static final long JWT_VERIFY_EXPIRATION = 300000L;
    private static final long JWT_ACCESS_EXPIRATION = 24 * 60 * 60 * 1000;
    private static final long JWT_REFRESH_EXPIRATION = 365L * 24 * 60 * 60 * 1000;

    public String generateVerifyToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_VERIFY_EXPIRATION);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }

    public String generateAccessToken(User user) {
        user.setPassword("");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_ACCESS_EXPIRATION);
        try {
            return Jwts.builder()
                    .claim("id", user.getId())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRole())
                    .setSubject(user.getId() + "")
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, JWT_SECRET.getBytes("UTF-8"))
                    .compact();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ApiRequestException("invalid_request", HttpStatus.UNAUTHORIZED);
        }

    }

    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_REFRESH_EXPIRATION);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_REFRESH_SECRET)
                .compact();
    }

    public User verifyToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        User user = new User();
        user.setId(Integer.parseInt(claims.get("id").toString()));
        user.setEmail(claims.get("email").toString());
        user.setRole(UserRole.valueOf(claims.get("role").toString()));
        return user;
    }

    public String verifyRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_REFRESH_SECRET)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

}