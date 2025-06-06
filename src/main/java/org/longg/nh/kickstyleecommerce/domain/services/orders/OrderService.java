package org.longg.nh.kickstyleecommerce.domain.services.orders;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.orders.CreateOrderRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderItemResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.OrderPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.*;
import org.longg.nh.kickstyleecommerce.domain.services.products.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IBaseService<Order, Long, OrderResponse, CreateOrderRequest, OrderResponse> {

  private final OrderPersistence orderPersistence;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductVariantRepository productVariantRepository;
  private final CouponRepository couponRepository;
  private final PaymentMethodRepository paymentMethodRepository;
  private final UserRepository userRepository;
  private final CouponUserRepository couponUserRepository;
  private final UserCouponUsageRepository userCouponUsageRepository;
  private final ProductService productService;

  @Override
  public IBasePersistence<Order, Long> getPersistence() {
    return orderPersistence;
  }

  @Transactional
  public OrderResponse createOrder(HeaderContext context, CreateOrderRequest request, Long userId) {
    // Lấy user
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "User không tồn tại"));

    // Validate payment method
    PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

    // Validate và tính toán order items
    List<OrderItem> orderItems = validateAndCreateOrderItems(request.getItems());
    BigDecimal subtotal = calculateSubtotal(orderItems);

    // Xử lý coupon nếu có
    Coupon coupon = null;
    BigDecimal discountAmount = BigDecimal.ZERO;
    
    if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
      coupon = validateAndUseCoupon(request.getCouponCode(), userId, subtotal);
      discountAmount = coupon.calculateDiscount(subtotal);
    }

    // Tính tổng tiền
    BigDecimal totalAmount = subtotal.subtract(discountAmount);

    // Tạo order
    Order order = Order.builder()
        .user(user)
        .code(orderRepository.generateOrderCode())
        .status(OrderStatus.PENDING)
        .customerName(request.getCustomerName())
        .customerEmail(request.getCustomerEmail())
        .customerPhone(request.getCustomerPhone())
        .shippingAddress(request.getShippingAddress())
        .shippingDistrict(request.getShippingDistrict())
        .shippingWard(request.getShippingWard())
        .subtotal(subtotal)
        .discountAmount(discountAmount)
        .totalAmount(totalAmount)
        .paymentMethod(paymentMethod)
        .paymentStatus(PaymentStatus.PENDING)
        .coupon(coupon)
        .couponCode(coupon != null ? coupon.getCode() : null)
        .note(request.getNote())
        .build();

    // Lưu order
    order = orderRepository.save(order);

    // Gán order cho các items và lưu
    for (OrderItem item : orderItems) {
      item.setOrder(order);
    }
    orderItemRepository.saveAll(orderItems);

    // Cập nhật stock và increment coupon usage
    updateProductStock(orderItems);
    
    if (coupon != null) {
      couponRepository.incrementUsedCount(coupon.getId());
      // Lưu lịch sử sử dụng coupon
      UserCouponUsage usage = UserCouponUsage.builder()
          .coupon(coupon)
          .user(user)
          .order(order)
          .build();
      userCouponUsageRepository.save(usage);
    }

    return mapToOrderResponse(order);
  }

  private List<OrderItem> validateAndCreateOrderItems(List<CreateOrderRequest.OrderItemRequest> itemRequests) {
    return itemRequests.stream().map(itemReq -> {
      ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
          .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
              "Product variant không tồn tại với ID: " + itemReq.getVariantId()));

      // Kiểm tra stock
      if (variant.getStockQuantity() == null || variant.getStockQuantity() < itemReq.getQuantity()) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, 
            "Không đủ hàng trong kho. Còn lại: " + (variant.getStockQuantity() != null ? variant.getStockQuantity() : 0));
      }

      // Tính giá
      BigDecimal unitPrice = variant.getProduct().getPrice();
      if (variant.getPriceAdjustment() != null && variant.getPriceAdjustment().compareTo(BigDecimal.ZERO) != 0) {
        unitPrice = unitPrice.add(variant.getPriceAdjustment());
      }

      // Tạo variant info để lưu snapshot
      Map<String, Object> variantInfo = new HashMap<>();
      variantInfo.put("sizeName", variant.getSize().getName());
      variantInfo.put("colorName", variant.getColor().getName());
      variantInfo.put("colorHexCode", variant.getColor().getHexCode());
      variantInfo.put("productCode", variant.getProduct().getCode());

      return OrderItem.builder()
          .variant(variant)
          .productName(variant.getProduct().getName())
          .variantInfo(variantInfo)
          .quantity(itemReq.getQuantity())
          .unitPrice(unitPrice)
          .build();
    }).collect(Collectors.toList());
  }

  private BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Coupon validateAndUseCoupon(String couponCode, Long userId, BigDecimal orderAmount) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    
    Coupon coupon = couponRepository.findValidCouponByCode(couponCode, now)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
            "Mã coupon không hợp lệ hoặc đã hết hạn"));

    // Kiểm tra coupon có giới hạn user không
    if (coupon.getUserSpecific() != null && coupon.getUserSpecific()) {
      boolean isAllowed = couponUserRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
      if (!isAllowed) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Bạn không được phép sử dụng mã coupon này");
      }
    }

    // Kiểm tra user đã sử dụng coupon này chưa
    boolean hasUsed = userCouponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
    if (hasUsed) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Bạn đã sử dụng mã coupon này rồi");
    }

    // Kiểm tra điều kiện minimum amount
    if (orderAmount.compareTo(coupon.getMinimumAmount()) < 0) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, 
          "Đơn hàng phải có giá trị tối thiểu " + coupon.getMinimumAmount() + " để sử dụng mã này");
    }

    return coupon;
  }

  private void updateProductStock(List<OrderItem> orderItems) {
    for (OrderItem item : orderItems) {
      ProductVariant variant = item.getVariant();
      int newStock = variant.getStockQuantity() - item.getQuantity();
      productService.updateVariantStock(variant.getId(), newStock);
    }
  }

  public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));

    order.setStatus(newStatus);
    order = orderRepository.save(order);
    
    return mapToOrderResponse(order);
  }

  public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));

    order.setPaymentStatus(newStatus);
    order = orderRepository.save(order);
    
    return mapToOrderResponse(order);
  }

  public List<OrderResponse> getOrdersByUser(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  public OrderResponse getOrderByCode(String code) {
    Order order = orderRepository.findByCode(code)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));
    
    return mapToOrderResponse(order);
  }

  public OrderResponse mapToOrderResponse(Order order) {
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
    
    List<OrderItemResponse> orderItemResponses = orderItems.stream()
        .map(item -> OrderItemResponse.builder()
            .id(item.getId())
            .variant(item.getVariant())
            .productName(item.getProductName())
            .variantInfo(item.getVariantInfo())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .createdAt(item.getCreatedAt())
            .build())
        .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(order.getId())
        .user(order.getUser())
        .code(order.getCode())
        .status(order.getStatus())
        .customerName(order.getCustomerName())
        .customerEmail(order.getCustomerEmail())
        .customerPhone(order.getCustomerPhone())
        .shippingAddress(order.getShippingAddress())
        .shippingDistrict(order.getShippingDistrict())
        .shippingWard(order.getShippingWard())
        .subtotal(order.getSubtotal())
        .discountAmount(order.getDiscountAmount())
        .totalAmount(order.getTotalAmount())
        .paymentMethod(order.getPaymentMethod())
        .paymentStatus(order.getPaymentStatus())
        .couponCode(order.getCouponCode())
        .note(order.getNote())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .orderItems(orderItemResponses)
        .build();
  }

  // Override để có đúng signature với Controller
  public void deleteById(HeaderContext context, Long id) {
    orderRepository.deleteById(id);
  }

  private BiFunction<HeaderContext, Order, OrderResponse> mappingResponseHandler() {
    return (context, order) -> mapToOrderResponse(order);
  }
} 