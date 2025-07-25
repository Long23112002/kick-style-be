package org.longg.nh.kickstyleecommerce.domain.services.auth;

import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.LoginRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.RegisterRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.ChangePasswordRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.UpdateUserRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.AuthResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.AccessToken;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.entities.Role;
import org.longg.nh.kickstyleecommerce.domain.entities.User;

import org.longg.nh.kickstyleecommerce.domain.repositories.CartRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.RoleRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.longg.nh.kickstyleecommerce.domain.services.cart.CartService;
import org.longg.nh.kickstyleecommerce.domain.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoderService passwordEncoderService;
  private final TokenService tokenService;
  private final EmailService emailService;
  private final CartRepository cartRepository;

  private static final String DEFAULT_ROLE_NAME = "USER";

  /** Đăng ký tài khoản mới */
  @Transactional
  public String register(RegisterRequest request) {
    log.info("Registering new user with email: {}", request.getEmail());

    // Validate email và phone không bị trùng
    validateUniqueEmailAndPhone(request.getEmail(), request.getPhone());

    // Lấy role mặc định
    Role defaultRole = getDefaultRole();

    // Tạo user mới
    User user =
        User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .password(passwordEncoderService.encode(request.getPassword()))
            .phone(request.getPhone())
            .address(request.getAddress())
            .district(request.getDistrict())
            .ward(request.getWard())
            .gender(request.getGender())
            .role(defaultRole)
            .isVerify(false)
            .isDeleted(false)
            .build();

    User savedUser = userRepository.save(user);

    initializeCart(savedUser);
    // Tạo verification token và gửi email
    AccessToken verificationToken = tokenService.generateVerificationToken(savedUser);
    emailService.sendVerificationEmail(
        savedUser.getEmail(), verificationToken.getToken(), savedUser.getFullName());

    log.info("User registered successfully: {}", savedUser.getEmail());
    return "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.";
  }

  private void initializeCart(User user) {
    Cart cart = new Cart();
    cart.setUserId(user.getId());
    cartRepository.save(cart);
  }

  /** Đăng nhập */
  @Transactional
  public AuthResponse login(LoginRequest request) {
    log.info("User login attempt: {}", request.getEmail());

    // Tìm user theo email
    User user =
        userRepository
            .findByEmailAndIsDeletedFalse(request.getEmail())
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác"));

    // Verify password
    if (!passwordEncoderService.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
    }

    // Check if user is verified
    if (!user.getIsVerify()) {
      throw new ResponseException(
          HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực. Vui lòng kiểm tra email.");
    }

    // Generate access token
    AccessToken accessToken = tokenService.generateAccessToken(user);

    // Build user response
    UserResponse userResponse = buildUserResponse(user);

    log.info("User logged in successfully: {}", user.getEmail());

    return AuthResponse.builder()
        .accessToken(accessToken.getToken())
        .tokenType("Bearer")
        .userInfo(userResponse)
        .expiresIn(tokenService.getAccessTokenExpirationInSeconds())
        .build();
  }

  /** Đăng xuất */
  @Transactional
  public String logout(String token) {
    log.info("User logout with token: {}", JwtUtils.maskToken(token));

    // Revoke token
    tokenService.revokeToken(token);

    log.info("User logged out successfully");
    return "Đăng xuất thành công";
  }

  /** Xác thực email */
  @Transactional
  public String verifyEmail(String token) {
    log.info("Email verification attempt with token: {}", JwtUtils.maskToken(token));

    AccessToken verificationToken =
        tokenService
            .validateVerificationToken(token)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Token xác thực không hợp lệ hoặc đã hết hạn"));

    User user = verificationToken.getUser();
    user.setIsVerify(true);
    userRepository.save(user);

    // Mark token as used
    tokenService.markVerificationTokenAsUsed(verificationToken);

    log.info("Email verified successfully for user: {}", user.getEmail());
    return "Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.";
  }

  /** Quên mật khẩu - gửi link reset */
  @Transactional
  public String forgotPassword(String email) {
    log.info("Password reset request for email: {}", email);

    User user =
        userRepository
            .findByEmailAndIsDeletedFalse(email)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.NOT_FOUND, "Email không tồn tại trong hệ thống"));

    // Revoke all existing tokens for security
    tokenService.revokeAllUserTokens(user);

    // Generate reset password token (JWT)
    AccessToken resetToken = tokenService.generateResetPasswordToken(user);
    emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken(), user.getFullName());

    log.info("Password reset email sent to: {}", email);
    return "Link đặt lại mật khẩu đã được gửi về email của bạn.";
  }

  /** Đặt lại mật khẩu */
  @Transactional
  public String resetPassword(String token, String newPassword) {
    log.info("Password reset attempt with token: {}", JwtUtils.maskToken(token));

    AccessToken resetToken =
        tokenService
            .validateVerificationToken(token)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST,
                        "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

    User user = resetToken.getUser();
    user.setPassword(passwordEncoderService.encode(newPassword));
    userRepository.save(user);

    // Mark token as used and revoke all access tokens
    tokenService.markVerificationTokenAsUsed(resetToken);
    tokenService.revokeAllUserTokens(user);

    log.info("Password reset successfully for user: {}", user.getEmail());
    return "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.";
  }

  /** Validate access token và trả về user info */
  public UserResponse getCurrentUser(String token) {
    AccessToken accessToken =
        tokenService
            .validateAccessToken(token)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn"));

    return buildUserResponse(accessToken.getUser());
  }

  /** Đổi mật khẩu (yêu cầu mật khẩu cũ) */
  @Transactional
  public String changePassword(String token, ChangePasswordRequest request) {
    log.info("Password change attempt with token: {}", JwtUtils.maskToken(token));

    // Validate access token và lấy user
    AccessToken accessToken =
        tokenService
            .validateAccessToken(token)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn"));

    User user = accessToken.getUser();

    // Validate mật khẩu cũ
    if (!passwordEncoderService.matches(request.getOldPassword(), user.getPassword())) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không chính xác");
    }

    // Validate mật khẩu mới và xác nhận mật khẩu
    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST, "Mật khẩu mới và xác nhận mật khẩu không khớp");
    }

    // Kiểm tra mật khẩu mới không được giống mật khẩu cũ
    if (passwordEncoderService.matches(request.getNewPassword(), user.getPassword())) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu cũ");
    }

    // Cập nhật mật khẩu mới
    user.setPassword(passwordEncoderService.encode(request.getNewPassword()));
    userRepository.save(user);

    // Revoke tất cả access tokens để bắt buộc đăng nhập lại
    tokenService.revokeAllUserTokens(user);

    log.info("Password changed successfully for user: {}", user.getEmail());
    return "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.";
  }

  /** Cập nhật thông tin người dùng mà không cần mật khẩu */
  @Transactional
  public UserResponse updateUserInfo( UpdateUserRequest request) {
    log.info("Updating user information for token: {}", JwtUtils.maskToken(request.getToken()));

    // Validate access token và lấy user
    AccessToken accessToken =
        tokenService
            .validateAccessToken(request.getToken())
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn"));
    Role userRole =
        roleRepository
            .findById(request.getRoleId())
            .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "Role không tồn tại"));

    User user = accessToken.getUser();

    if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
      if (userRepository.existsByPhone(request.getPhone())) {
        throw new ResponseException(
            HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng bởi tài khoản khác");
      }
      user.setPhone(request.getPhone());
    }

    // Cập nhật các thông tin khác
    if (request.getFullName() != null) {
      user.setFullName(request.getFullName());
    }

    if (request.getAddress() != null) {
      user.setAddress(request.getAddress());
    }

    if (request.getDistrict() != null) {
      user.setDistrict(request.getDistrict());
    }

    if (request.getWard() != null) {
      user.setWard(request.getWard());
    }

    if (request.getGender() != null) {
      user.setGender(request.getGender());
    }

    if (request.getAvatarUrl() != null) {
      user.setAvatarUrl(request.getAvatarUrl());
    }
    user.setRole(userRole);

    // Lưu thông tin đã cập nhật
    User updatedUser = userRepository.save(user);
    log.info("User information updated successfully for: {}", updatedUser.getEmail());

    return buildUserResponse(updatedUser);
  }

  // Helper methods
  private void validateUniqueEmailAndPhone(String email, String phone) {
    if (userRepository.existsByEmail(email)) {
      throw new ResponseException(HttpStatus.CONFLICT, "Email đã được sử dụng");
    }

    if (phone != null && userRepository.existsByPhone(phone)) {
      throw new ResponseException(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng");
    }
  }

  private Role getDefaultRole() {
    return roleRepository
        .findByName(DEFAULT_ROLE_NAME)
        .orElseThrow(
            () ->
                new ResponseException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Role mặc định không tồn tại"));
  }

  private UserResponse buildUserResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .cartId(
            cartRepository
                .findByUserId(user.getId())
                .orElseThrow(
                    () ->
                        new ResponseException(
                            HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng cho user"))
                .getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .address(user.getAddress())
        .district(user.getDistrict())
        .ward(user.getWard())
        .role(user.getRole())
        .avatarUrl(user.getAvatarUrl())
        .gender(user.getGender())
        .isVerify(user.getIsVerify())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
