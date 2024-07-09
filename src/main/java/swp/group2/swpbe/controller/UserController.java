package swp.group2.swpbe.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import swp.group2.swpbe.dto.ChangePasswordDTO;
import swp.group2.swpbe.dto.LoginDTO;
import swp.group2.swpbe.dto.LoginSocialDTO;
import swp.group2.swpbe.dto.ResetPasswordDTO;
import swp.group2.swpbe.dto.ReverifyDTO;
import swp.group2.swpbe.dto.SignupDTO;
import swp.group2.swpbe.dto.UpdatePasswordDTO;
import swp.group2.swpbe.dto.UpdateProfileDTO;
import swp.group2.swpbe.entities.User;
import swp.group2.swpbe.exception.ApiRequestException;
import swp.group2.swpbe.response.AuthResponse;
import swp.group2.swpbe.service.AuthService;
import swp.group2.swpbe.service.CloudinaryService;
import swp.group2.swpbe.service.JwtService;
import swp.group2.swpbe.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final AuthService authService;
    private final JwtService jwtService;

    public UserController(UserService userService, AuthService authService,
            CloudinaryService cloudinaryService, JwtService jwtService) {
        this.userService = userService;
        this.authService = authService;
        this.cloudinaryService = cloudinaryService;
        this.jwtService = jwtService;
    }

    @Value("${allow.origin}")
    private String allowedOrigins;

    @PostMapping("auth/signup")
    public User create(@RequestBody SignupDTO body) {
        return userService.signup(body);
    }

    @GetMapping("auth/refresh")
    public String refresh(@RequestHeader("Authorization") String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("auth/login")
    public AuthResponse login(@RequestBody LoginDTO body) {
        User user = userService.login(body);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getId() + "");
        return new AuthResponse(user, accessToken, refreshToken);
    }

    @PostMapping("auth/forgot-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO body) {
        userService.forgotPassword(body);
        return ResponseEntity.ok("Email sent successfully.");

    }

    @PatchMapping("auth/reset-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO body) {
        userService.changePassword(body);
        return ResponseEntity.ok("Reset password successfully.");

    }

    @PostMapping("auth/social")
    public AuthResponse loginSocial(@RequestBody LoginSocialDTO body) {
        User user = userService.saveSocialUser(body);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getId() + "");
        return new AuthResponse(user, accessToken, refreshToken);
    }

    @GetMapping("auth/verify")
    public void verifyEmail(@RequestParam(name = "token") String query, HttpServletResponse response) {
        userService.updateVerifyEmail(query);
        try {
            response.sendRedirect(allowedOrigins + "/login");
        } catch (Exception e) {
            throw new ApiRequestException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("auth/reverify")
    public ResponseEntity<?> reverify(@RequestBody ReverifyDTO body) {
        userService.reverify(body.getEmail());
        return ResponseEntity.ok("Reverification email sent successfully.");

    }

    @GetMapping("auth/profile")
    public User getProfile(@RequestHeader("Authorization") String token) {
        String userId = authService.loginUser(token).getId() + "";
        return userService.getUserProfile(userId);
    }

    @PatchMapping("auth/change-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordDTO body,
            @RequestHeader("Authorization") String token) {
        String userId = authService.loginUser(token).getId() + "";
        userService.updatePassword(body, userId);
        return ResponseEntity.ok("Update password successfully");
    }

    @PatchMapping("auth/update-avatar")
    public ResponseEntity<?> update(@RequestParam("image") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        String userId = authService.loginUser(token).getId() + "";
        Map data = this.cloudinaryService.upload(file);
        String url = (String) data.get("url");
        userService.updateAvatar(url, userId);
        return new ResponseEntity<>("update avatar successfully", HttpStatus.OK);
    }

    @PutMapping("auth/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileDTO body,
            @RequestHeader("Authorization") String token) {
        String userId = authService.loginUser(token).getId() + "";
        userService.updateProfile(body, userId);
        return new ResponseEntity<>("update profile successfully", HttpStatus.OK);
    }

}