package org.longg.nh.kickstyleecommerce.domain.services.orders;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;

import com.eps.shared.utils.FnCommon;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.orders.CreateOrderRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderItemResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.OrderPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.*;
import org.longg.nh.kickstyleecommerce.domain.services.products.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService
    implements IBaseService<Order, Long, OrderResponse, CreateOrderRequest, OrderResponse> {

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
  private final CartItemRepository cartItemRepository;
  private final ReviewsRepository reviewRepository;
  private final ProductRepository productRepository;

  @Override
  public IBasePersistence<Order, Long> getPersistence() {
    return orderPersistence;
  }

  public Order finById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));
  }

  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    Page<Order> orders = orderRepository.findAll(pageable);
    return orders.map(this::mapToOrderResponse);
  }

  public Boolean checkOrderReview(Long userId, Long orderId) {
    return reviewRepository.existsByUserIdAndOrderId(userId, orderId);
  }

  @Transactional
  public OrderResponse createOrder(HeaderContext context, CreateOrderRequest request, Long userId) {
    // Lấy user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "User không tồn tại"));

    // Validate payment method
    PaymentMethod paymentMethod =
        paymentMethodRepository
            .findById(request.getPaymentMethodId())
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

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
    Order order =
        Order.builder()
            .user(user)
            .code("OD" + orderRepository.getNextSequence())
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
            .isDeleted(false)
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
      UserCouponUsage usage =
          UserCouponUsage.builder().coupon(coupon).user(user).order(order).build();
      userCouponUsageRepository.save(usage);
    }

    // Xử lý cart items - Xóa hoặc trừ số lượng các variant đã order
    if (request.getCartId() != null) {
      List<CartItem> cartItems = cartItemRepository.findByCartId(request.getCartId());
      List<CartItem> cartItemsToDelete = new ArrayList<>();
      List<CartItem> cartItemsToUpdate = new ArrayList<>();

      for (CartItem cartItem : cartItems) {
        // Tìm order item tương ứng với cart item này
        OrderItem matchingOrderItem =
            orderItems.stream()
                .filter(
                    orderItem ->
                        orderItem.getVariant().getId().equals(cartItem.getVariant().getId()))
                .findFirst()
                .orElse(null);

        if (matchingOrderItem != null) {
          int orderedQuantity = matchingOrderItem.getQuantity();
          int cartQuantity = cartItem.getQuantity();

          if (cartQuantity <= orderedQuantity) {
            cartItemsToDelete.add(cartItem);
          } else {
            cartItem.setQuantity(cartQuantity - orderedQuantity);
            cartItemsToUpdate.add(cartItem);
          }
        }
      }

      if (!cartItemsToDelete.isEmpty()) {
        cartItemRepository.deleteAll(cartItemsToDelete);
      }
      if (!cartItemsToUpdate.isEmpty()) {
        cartItemRepository.saveAll(cartItemsToUpdate);
      }
    }

    return mapToOrderResponse(order);
  }

  private List<OrderItem> validateAndCreateOrderItems(
      List<CreateOrderRequest.OrderItemRequest> itemRequests) {
    return itemRequests.stream()
        .map(
            itemReq -> {
              ProductVariant variant =
                  productVariantRepository
                      .findById(itemReq.getVariantId())
                      .orElseThrow(
                          () ->
                              new ResponseException(
                                  HttpStatus.BAD_REQUEST,
                                  "Product variant không tồn tại với ID: "
                                      + itemReq.getVariantId()));

              // Kiểm tra stock
              if (variant.getStockQuantity() == null
                  || variant.getStockQuantity() < itemReq.getQuantity()) {
                throw new ResponseException(
                    HttpStatus.BAD_REQUEST,
                    "Không đủ hàng trong kho. Còn lại: "
                        + (variant.getStockQuantity() != null ? variant.getStockQuantity() : 0));
              }

              // Tính giá
              BigDecimal unitPrice = variant.getProduct().getPrice();
              if (variant.getPriceAdjustment() != null
                  && variant.getPriceAdjustment().compareTo(BigDecimal.ZERO) != 0) {
                unitPrice = unitPrice.add(variant.getPriceAdjustment());
              }

              // Tạo variant info để lưu snapshot
              Map<String, Object> variantInfo = new HashMap<>();
              variantInfo.put("sizeName", variant.getSize().getName());
              variantInfo.put("colorName", variant.getColor().getName());
              variantInfo.put("productCode", variant.getProduct().getCode());

              return OrderItem.builder()
                  .variant(variant)
                  .productName(variant.getProduct().getName())
                  .variantInfo(variantInfo)
                  .quantity(itemReq.getQuantity())
                  .unitPrice(unitPrice)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
    return orderItems.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Coupon validateAndUseCoupon(String couponCode, Long userId, BigDecimal orderAmount) {
    Timestamp now = new Timestamp(System.currentTimeMillis());

    Coupon coupon =
        couponRepository
            .findValidCouponByCode(couponCode, now)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Mã coupon không hợp lệ hoặc đã hết hạn"));

    // Kiểm tra coupon có giới hạn user không
    if (coupon.getUserSpecific() != null && coupon.getUserSpecific()) {
      boolean isAllowed = couponUserRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
      if (!isAllowed) {
        throw new ResponseException(
            HttpStatus.BAD_REQUEST, "Bạn không được phép sử dụng mã coupon này");
      }
    }

    // Kiểm tra user đã sử dụng coupon này chưa
    boolean hasUsed = userCouponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId);
    if (hasUsed) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Bạn đã sử dụng mã coupon này rồi");
    }

    // Kiểm tra điều kiện minimum amount
    if (orderAmount.compareTo(coupon.getMinimumAmount()) < 0) {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST,
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
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));

    order.setStatus(newStatus);
    order = orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus newStatus) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));

    order.setPaymentStatus(newStatus);
    order = orderRepository.save(order);

    return mapToOrderResponse(order);
  }

  public List<OrderResponse> getOrdersByUser(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  public OrderResponse getOrderByCode(String code) {
    Order order =
        orderRepository
            .findByCode(code)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));

    return mapToOrderResponse(order);
  }

  public OrderResponse mapToOrderResponse(Order order) {
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

    List<OrderItemResponse> orderItemResponses =
        orderItems.stream()
            .map(
                item ->
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
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
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
        .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
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
        .paymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null)
        .paymentMethodName(
            order.getPaymentMethod() != null ? order.getPaymentMethod().getName() : null)
        .paymentStatus(order.getPaymentStatus())
        .couponCode(order.getCouponCode())
        .note(order.getNote())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .orderItems(orderItemResponses)
        .isReviewed(
            checkOrderReview(
                order.getUser() != null ? order.getUser().getId() : null, order.getId()))
        .build();
  }

  // Override để có đúng signature với Controller
  public void deleteById(HeaderContext context, Long id) {
    orderRepository.deleteById(id);
  }


  public byte[] generateOrderPdf(Long orderId) {
    Order order = finById(orderId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {

      InputStream fontStream =
              getClass().getClassLoader().getResourceAsStream("msttcorefonts/Times_New_Roman.ttf");
      if (fontStream == null) {
        throw new FileNotFoundException("Font file not found in classpath!");
      }

      byte[] fontBytes = fontStream.readAllBytes();
      var fontProgram = FontProgramFactory.createFont(fontBytes);
      PdfFont font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H);

      PdfWriter writer = new PdfWriter(out);
      PdfDocument pdf = new PdfDocument(writer);
      pdf.setDefaultPageSize(new PageSize(200, 600));
      Document document = new Document(pdf);

      document.setFont(font);
      document.setFontSize(8);

      InputStream logoStream =
              getClass().getClassLoader().getResourceAsStream("msttcorefonts/logo.png");
      if (logoStream == null) {
        throw new FileNotFoundException("Logo file not found in classpath!");
      }

      byte[] logoBytes = logoStream.readAllBytes();

      ImageData imageData = ImageDataFactory.create(logoBytes);

      Image logo = new Image(imageData);
      logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
      logo.setWidth(50);
      logo.setHeight(50);
      document.add(logo);

      document.add(
              new Paragraph("HÓA ĐƠN BÁN HÀNG")
                      .setTextAlignment(TextAlignment.CENTER)
                      .setBold()
                      .setMarginBottom(10));

      document.add(new Paragraph("Mã đơn hàng: " + order.getCode()).setBold());
      document.add(new Paragraph("Khách hàng: " + order.getCustomerName()).setBold());
      if (order.getCustomerPhone() != null) {
        document.add(new Paragraph("Số ĐT: " + order.getCustomerPhone()).setBold());
      }
      LocalDateTime createdAt = order.getCreatedAt().toLocalDateTime();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
      String formattedDate = createdAt.format(formatter);
      document.add(new Paragraph("Ngày mua: " + formattedDate).setBold());

      Table table = new Table(new float[] {1, 7, 2, 3});
      table.setWidth(UnitValue.createPercentValue(100));
      table.addHeaderCell(
              new Cell().add(new Paragraph("STT").setBold()).setTextAlignment(TextAlignment.CENTER));
      table.addHeaderCell(
              new Cell()
                      .add(new Paragraph("Tên sản phẩm").setBold())
                      .setTextAlignment(TextAlignment.LEFT));
      table.addHeaderCell(
              new Cell().add(new Paragraph("SL").setBold()).setTextAlignment(TextAlignment.CENTER));
      table.addHeaderCell(
              new Cell()
                      .add(new Paragraph("Thành tiền").setBold())
                      .setTextAlignment(TextAlignment.RIGHT));

      int index = 1;
      double totalWithoutDiscount = 0.0;
      for (var detail : order.getOrderItems()) {
        log.info("Detail: {}", detail.getVariantInfo());
        BigDecimal lineTotal = detail.getTotalPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        Product product = productRepository.findByCode(detail.getVariantInfo().get("productCode").toString())
                .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Sản phẩm không tồn tại"));

        table.addCell(
                new Cell()
                        .add(new Paragraph(String.valueOf(index++)))
                        .setTextAlignment(TextAlignment.CENTER));
        table.addCell(
                new Cell()
                        .add(new Paragraph(product.getName()))
                        .setTextAlignment(TextAlignment.LEFT));
        table.addCell(
                new Cell()
                        .add(new Paragraph(String.valueOf(detail.getQuantity())))
                        .setTextAlignment(TextAlignment.CENTER));
        table.addCell(
                new Cell()
                        .add(new Paragraph(String.format("%,.0f", lineTotal)))
                        .setTextAlignment(TextAlignment.RIGHT));
      }
      document.add(table);

      BigDecimal discount = order.getDiscountAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
//      double totalWithDiscount = od.getDiscountAmount();

      document.add(
              new Paragraph("Tổng tiền hoá đơn: " + String.format("%,.0f VNĐ", order.getTotalAmount()))
                      .setTextAlignment(TextAlignment.RIGHT)
                      .setBold()
                      .setMarginTop(10));

      if (discount.compareTo(BigDecimal.ZERO) > 0) {
        document.add(
                new Paragraph("Khuyến mãi: -" + String.format("%,.0f VNĐ", order.getDiscountAmount()))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBold());
      }

//      document
//              .add(
//                      new Paragraph("Phí giao hàng: " + String.format("%,.0f VNĐ", order.get()))
//                              .setTextAlignment(TextAlignment.RIGHT)
//                              .setBold())
//              .setTopMargin(10);
      document
              .add(
                      new Paragraph("Số tiền thanh toán: " + String.format("%,.0f VNĐ", order.getTotalAmount()))
                              .setTextAlignment(TextAlignment.RIGHT)
                              .setBold())
              .setTopMargin(10);

      document.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Lỗi xuất hoá đơn");
    }

    return out.toByteArray();
  }




}
