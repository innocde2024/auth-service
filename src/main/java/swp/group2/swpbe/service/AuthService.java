package swp.group2.swpbe.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.ExpiredJwtException;
import swp.group2.swpbe.constant.UserRole;
import swp.group2.swpbe.entities.User;
import swp.group2.swpbe.exception.ApiRequestException;
import swp.group2.swpbe.repository.UserRepository;
import swp.group2.swpbe.response.UserDisplayResponse;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User loginUser(String token) {
        if (token == null) {
            throw new ApiRequestException("invalid_request", HttpStatus.UNAUTHORIZED);
        }
        User user = null;
        try {
            user = jwtService.verifyToken(token);
        } catch (ExpiredJwtException e) {
            throw new ApiRequestException("expired_session", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    public String refreshToken(String token) {
        if (token == null) {
            throw new ApiRequestException("invalid_request", HttpStatus.UNAUTHORIZED);
        }
        try {
            String userId = jwtService.verifyRefreshToken(token);
            User user = userRepository.findById(Integer.parseInt(userId));
            return jwtService.generateAccessToken(user);
        } catch (ExpiredJwtException e) {
            throw new ApiRequestException("expired_session", HttpStatus.UNAUTHORIZED);
        }

    }

    public boolean isAdmin(String userId) {
        User user = userRepository.findById(Integer.parseInt(userId));
        return user.getRole().equals(UserRole.ADMIN);
    }

    public List<UserDisplayResponse> getUserDisplayInformation() {
        List<User> users = userRepository.findAll();
        List<UserDisplayResponse> userDisplays = new ArrayList<>();
        for (User user : users) {
            userDisplays.add(
                    new UserDisplayResponse(user.getId(), user.getFullName(), user.getEmail(), user.getAvatarUrl()));
        }
        return userDisplays;

    }
}
