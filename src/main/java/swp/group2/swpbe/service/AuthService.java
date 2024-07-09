package swp.group2.swpbe.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.ExpiredJwtException;
import swp.group2.swpbe.constant.UserRole;
import swp.group2.swpbe.entities.User;
import swp.group2.swpbe.exception.ApiRequestException;
import swp.group2.swpbe.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String loginUser(String token) {
        if (token == null) {
            throw new ApiRequestException("invalid_request", HttpStatus.UNAUTHORIZED);
        }
        String userId = null;
        try {
            userId = jwtService.verifyToken(token);
        } catch (ExpiredJwtException e) {
            throw new ApiRequestException("expired_session", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }

    public String refreshToken(String token) {
        if (token == null) {
            throw new ApiRequestException("invalid_request", HttpStatus.UNAUTHORIZED);
        }
        try {
            String userId = jwtService.verifyToken(token);
            return jwtService.generateAccessToken(userId);
        } catch (ExpiredJwtException e) {
            throw new ApiRequestException("expired_session", HttpStatus.UNAUTHORIZED);
        }

    }

    public boolean isAdmin(String userId) {
        User user = userRepository.findById(Integer.parseInt(userId));
        return user.getRole().equals(UserRole.ADMIN);
    }
}
