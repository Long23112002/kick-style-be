package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.UserRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.persistence.UserPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartRepository;
import org.longg.nh.kickstyleecommerce.domain.services.auth.PasswordEncoderService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService
    implements IBaseService<User, Long, UserResponse, UserRequest, UserResponse> {

  private final UserPersistence userPersistence;
  private final PasswordEncoderService passwordEncoderService;
  private final CartRepository cartRepository;
  private final RoleService roleService;

  @Override
  public IBasePersistence<User, Long> getPersistence() {
    return userPersistence;
  }

  @Override
  public void postCreateHandler(HeaderContext context, User entity, UserRequest request) {
    if (request.getIsAdmin()) {
      entity.setIsVerify(true);
    } else {
      entity.setIsVerify(false);
    }
    entity.setPassword(passwordEncoderService.encode(request.getPassword()));
    entity.setRole(roleService.getEntityById(context, request.getRoleId()));
    initializeCart(entity);
    IBaseService.super.postCreateHandler(context, entity, request);
  }

  @Override
  public void postUpdateHandler(
      HeaderContext context, User originalEntity, User entity, Long aLong, UserRequest request) {

    // Cập nhật Role mới
    entity.setRole(roleService.getEntityById(context, request.getRoleId()));

    // Chỉ cập nhật email nếu thay đổi
    String newEmail = request.getEmail();
    String currentEmail = originalEntity.getEmail();

    if (newEmail != null && !newEmail.equalsIgnoreCase(currentEmail)) {
      entity.setEmail(newEmail);
    } else {
      // Giữ lại email cũ nếu không thay đổi
      entity.setEmail(currentEmail);
    }

    // Gọi xử lý mặc định của lớp cha
    IBaseService.super.postUpdateHandler(context, originalEntity, entity, aLong, request);
  }

  private void initializeCart(User user) {
    Cart cart = new Cart();
    cart.setUserId(user.getId());
    cartRepository.save(cart);
  }
}
