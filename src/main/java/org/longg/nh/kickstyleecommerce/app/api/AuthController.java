package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.annotations.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.LoginRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.RegisterRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.AuthResponse;
import org.longg.nh.kickstyleecommerce.domain.services.auth.AuthService;
import org.longg.nh.kickstyleecommerce.domain.utils.JwtUtils;
import org.longg.nh.kickstyleecommerce.infrastructure.config.annotation.CheckRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /** Đăng ký tài khoản mới */
  @PostMapping("/register")
  public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
    String result = authService.register(request);
    return ResponseEntity.ok(result);
  }

  /** Đăng nhập */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  /** Đăng xuất */
  @PostMapping("/logout")
  @RolesAllowed("*")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
    String token = JwtUtils.extractTokenFromHeader(authHeader);
    String result = authService.logout(token);
    return ResponseEntity.ok(result);
  }

  /** Xác thực email */
  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
    String result = authService.verifyEmail(token);
    return ResponseEntity.ok(result);
  }

  /** Quên mật khẩu */
  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
    String result = authService.forgotPassword(email);
    return ResponseEntity.ok(result);
  }

  /** Đặt lại mật khẩu */
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(
      @RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
    String result = authService.resetPassword(token, newPassword);
    return ResponseEntity.ok(result);
  }

  /** Lấy thông tin user hiện tại */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(
      @RequestHeader("Authorization") String authHeader) {
    String token = JwtUtils.extractTokenFromHeader(authHeader);
    UserResponse userResponse = authService.getCurrentUser(token);
    return ResponseEntity.ok(userResponse);
  }
}
