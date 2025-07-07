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
import org.longg.nh.kickstyleecommerce.domain.dtos.OrderParam;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.orders.CreateOrderRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.coupons.CouponResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderItemResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.OrderPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.*;
import org.longg.nh.kickstyleecommerce.domain.services.auth.EmailService;
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
import java.util.Optional;

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
  private final EmailService emailService;

  @Override
  public IBasePersistence<Order, Long> getPersistence() {
    return orderPersistence;
  }

  public Order finById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Đơn hàng không tồn tại"));
  }

  public Page<OrderResponse> filter(OrderParam param, Pageable pageable) {
    Page<Order> orders = orderRepository.filter( param,pageable);
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
            .totalAmount(request.getTotalAmount())
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
                BigDecimal unitPrice = BigDecimal.ZERO;

                if (variant.getPriceAdjustment() != null
                        && variant.getPriceAdjustment().compareTo(BigDecimal.ZERO) != 0) {
                    unitPrice = variant.getPriceAdjustment();
                }


                // Tạo variant info để lưu snapshot
              Map<String, Object> variantInfo = new HashMap<>();
              variantInfo.put("sizeName", variant.getSize().getName());
              variantInfo.put("colorName", variant.getColor().getName());
              variantInfo.put("productCode", variant.getProduct().getCode());
              // Lưu cả productId để sau này có thể dùng findProductForOrderById
              variantInfo.put("productId", variant.getProduct().getId());
              // Lưu variantId để có thể dễ dàng tham chiếu sau này
              variantInfo.put("variantId", variant.getId());
              // Lưu tên sản phẩm
              variantInfo.put("productName", variant.getProduct().getName());
              // Lưu thêm thông tin hình ảnh của sản phẩm
              variantInfo.put("productImages", variant.getProduct().getImageUrls());
              // Lưu thêm thông tin slug của sản phẩm
              variantInfo.put("productSlug", variant.getProduct().getSlug());
              // Lưu giá sản phẩm
              variantInfo.put("productPrice", variant.getProduct().getPrice());
              variantInfo.put("productSalePrice", variant.getProduct().getSalePrice());

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

  /**
   * Lấy danh sách đơn hàng của một user, đảm bảo thông tin sản phẩm được trả về đầy đủ
   * kể cả khi sản phẩm đã bị xóa mềm
   */
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
    // Sử dụng findByOrderIdNative để lấy thông tin OrderItems bỏ qua soft-delete filters
    List<OrderItem> orderItems = orderItemRepository.findByOrderIdNative(order.getId());
    
    // Tạo danh sách product codes từ orderItems
    List<String> productCodes = orderItems.stream()
        .filter(item -> item.getVariantInfo() != null 
            && item.getVariantInfo().containsKey("productCode"))
        .map(item -> item.getVariantInfo().get("productCode").toString())
        .distinct()
        .collect(Collectors.toList());
    
    // Lấy tất cả products cùng lúc
    Map<String, Product> productsByCode = new HashMap<>();
    if (!productCodes.isEmpty()) {
        productRepository.findProductsByCodesIncludingDeleted(productCodes).forEach(product -> 
            productsByCode.put(product.getCode(), product));
    }

    List<OrderItemResponse> orderItemResponses =
        orderItems.stream()
            .map(
                item -> {
                    Long productId = null;
                    Long variantId = null;
                    
                    // Lấy variant ID
                    if (item.getVariant() != null) {
                        variantId = item.getVariant().getId();
                    } else if (item.getVariantInfo() != null && item.getVariantInfo().containsKey("variantId")) {
                        try {
                            variantId = Long.valueOf(item.getVariantInfo().get("variantId").toString());
                        } catch (Exception e) {
                            log.error("Error parsing variantId from variantInfo: {}", e.getMessage());
                        }
                    }
                    
                    // Khởi tạo các giá trị mặc định
                    List<String> productImages = null;
                    String productSlug = null;
                    BigDecimal productPrice = null;
                    BigDecimal productSalePrice = null;
                    String productName = item.getProductName(); // Mặc định lấy từ OrderItem
                    
                    // Lấy productId và thông tin product từ code
                    Product foundProduct = null;
                    if (item.getVariantInfo() != null && item.getVariantInfo().containsKey("productCode")) {
                        String productCode = item.getVariantInfo().get("productCode").toString();
                        foundProduct = productsByCode.get(productCode);
                        
                        if (foundProduct != null) {
                            productId = foundProduct.getId();
                            productImages = foundProduct.getImageUrls();
                            productSlug = foundProduct.getSlug();
                            productPrice = foundProduct.getPrice();
                            productSalePrice = foundProduct.getSalePrice();
                            productName = foundProduct.getName();
                        }
                    }
                    
                    // Nếu không tìm thấy từ code, thử lấy từ variantInfo
                    if (foundProduct == null) {
                        if (item.getVariantInfo() != null && item.getVariantInfo().containsKey("productId")) {
                            try {
                                productId = Long.valueOf(item.getVariantInfo().get("productId").toString());
                                
                                Optional<Product> productOpt = productRepository.findProductByIdIncludingDeleted(productId);
                                if (productOpt.isPresent()) {
                                    Product product = productOpt.get();
                                    productImages = product.getImageUrls();
                                    productSlug = product.getSlug();
                                    productPrice = product.getPrice();
                                    productSalePrice = product.getSalePrice();
                                    productName = product.getName();
                                }
                            } catch (Exception e) {
                                log.warn("Error fetching product for ID from variantInfo: {}", e.getMessage());
                            }
                        }
                    }
                    
                    // Nếu vẫn không tìm được thông tin product, thử lấy từ variant
                    if (productId == null && item.getVariant() != null && item.getVariant().getProduct() != null) {
                        try {
                            Product product = item.getVariant().getProduct();
                            productId = product.getId();
                            productImages = product.getImageUrls();
                            productSlug = product.getSlug();
                            productPrice = product.getPrice();
                            productSalePrice = product.getSalePrice();
                            productName = product.getName();
                        } catch (Exception e) {
                            log.warn("Error getting product from variant: {}", e.getMessage());
                        }
                    }
                    
                    // Đảm bảo productId không null trong response
                    if (productId == null) {
                        log.warn("Could not determine productId for order item {}, using placeholder", item.getId());
                        productId = -1L; // Placeholder ID để không bị null
                    }
                    
                    // Lấy thông tin từ variantInfo nếu vẫn chưa có
                    if (productImages == null && item.getVariantInfo() != null && item.getVariantInfo().containsKey("productImages")) {
                        try {
                            productImages = (List<String>) item.getVariantInfo().get("productImages");
                        } catch (Exception e) {
                            log.error("Error retrieving product images from variantInfo: {}", e.getMessage());
                        }
                    }
                    
                    if (productSlug == null && item.getVariantInfo() != null && item.getVariantInfo().containsKey("productSlug")) {
                        productSlug = item.getVariantInfo().get("productSlug").toString();
                    }
                    
                    if (productPrice == null && item.getVariantInfo() != null && item.getVariantInfo().containsKey("productPrice")) {
                        try {
                            Object priceObj = item.getVariantInfo().get("productPrice");
                            if (priceObj instanceof BigDecimal) {
                                productPrice = (BigDecimal) priceObj;
                            } else if (priceObj instanceof Number) {
                                productPrice = BigDecimal.valueOf(((Number) priceObj).doubleValue());
                            } else if (priceObj instanceof String) {
                                productPrice = new BigDecimal((String) priceObj);
                            }
                        } catch (Exception e) {
                            log.error("Error retrieving product price from variantInfo: {}", e.getMessage());
                        }
                    }
                    
                    // Đảm bảo các giá trị không bị null
                    if (productImages == null) {
                        productImages = new ArrayList<>();
                    }
                    
                    if (productSlug == null) {
                        productSlug = "deleted-product";
                    }
                    
                    if (productPrice == null) {
                        // Sử dụng unitPrice của OrderItem nếu không có giá khác
                        productPrice = item.getUnitPrice();
                    }
                    
                    if (productSalePrice == null) {
                        // Nếu không có giá khuyến mãi, dùng giá chính
                        productSalePrice = productPrice;
                    }
                    
                    if (productName == null || productName.isEmpty()) {
                        productName = "Sản phẩm đã xóa";
                    }
                    
                    return OrderItemResponse.builder()
                        .id(item.getId())
                        .variantId(variantId)
                        .productName(productName)
                        .variantInfo(item.getVariantInfo())
                        .productId(productId)
                        .productImages(productImages)
                        .productSlug(productSlug)
                        .productPrice(productPrice)
                        .productSalePrice(productSalePrice)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .createdAt(item.getCreatedAt())
                        .build();
                })
            .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(order.getId())
        .userId(order.getUser() != null ? order.getUser().getId() : null)
        .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
        .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
        .code(order.getCode())
        .status(order.getStatus())
            .coupon(order.getCoupon() != null ? mapToCouponResponse(order.getCoupon()) : null)
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
        .note(order.getNote())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .orderItems(orderItemResponses)
        .isReviewed(
            checkOrderReview(
                order.getUser() != null ? order.getUser().getId() : null, order.getId()))
        .build();
  }

    public CouponResponse mapToCouponResponse(Coupon coupon) {
        List<Long> userIds = null;

        if (coupon.getUserSpecific() != null && coupon.getUserSpecific()) {
            userIds = couponUserRepository.findUserIdsByCouponId(coupon.getId());
        }

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumAmount(coupon.getMinimumAmount())
                .maximumDiscount(coupon.getMaximumDiscount())
                .maxUsageCount(coupon.getMaxUsageCount())
                .usedCount(coupon.getUsedCount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .validFrom(coupon.getValidFrom())
                .validTo(coupon.getValidTo())
                .userSpecific(coupon.getUserSpecific())
                .isActive(coupon.getIsActive())
                .userIds(userIds)
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
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
        BigDecimal lineTotal =
            detail.getTotalPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        
        // Sử dụng findByCode mới có thể truy vấn được cả product đã soft-deleted
        Product product = null;
        try {
            // Trước tiên thử tìm bằng code từ repository, sử dụng phương thức findByCodeIncludingDeleted 
            // để lấy cả sản phẩm đã soft-deleted
            product = productRepository
                .findByCodeIncludingDeleted(detail.getVariantInfo().get("productCode").toString())
                .orElse(null);
                
            // Nếu không tìm thấy và có productId trong variantInfo, thử tìm bằng ID kể cả đã xóa
            if (product == null && detail.getVariantInfo().containsKey("productId")) {
                Long productId = Long.valueOf(detail.getVariantInfo().get("productId").toString());
                product = productService.findProductForOrderById(productId);
            }
        } catch (Exception e) {
            // Nếu lỗi, tạo thông tin sản phẩm mặc định
            log.error("Error retrieving product for order PDF: {}", e.getMessage());
        }
        
        String productName = detail.getProductName(); // Dùng tên từ order item nếu không tìm thấy product
        if (product != null) {
            productName = product.getName();
        }

        table.addCell(
            new Cell()
                .add(new Paragraph(String.valueOf(index++)))
                .setTextAlignment(TextAlignment.CENTER));
        table.addCell(
            new Cell().add(new Paragraph(productName)).setTextAlignment(TextAlignment.LEFT));
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

      BigDecimal discount =
          order.getDiscountAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
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
      //                      new Paragraph("Phí giao hàng: " + String.format("%,.0f VNĐ",
      // order.get()))
      //                              .setTextAlignment(TextAlignment.RIGHT)
      //                              .setBold())
      //              .setTopMargin(10);
      document
          .add(
              new Paragraph(
                      "Số tiền thanh toán: " + String.format("%,.0f VNĐ", order.getTotalAmount()))
                  .setTextAlignment(TextAlignment.RIGHT)
                  .setBold())
          .setTopMargin(10);

      document.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Lỗi xuất hoá đơn");
    }

    byte[] pdf = out.toByteArray();

    emailService.sendOrderPdfEmail(
        order.getUser().getEmail(),
        order.getUser().getFullName(),
        pdf,
        "hoa-don-" + order.getCode() + ".pdf");

    return pdf;
  }

  private OrderItem createOrderItemFromCartItem(CartItem cartItem, Order order) {
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setVariant(cartItem.getVariant());
    orderItem.setQuantity(cartItem.getQuantity());
    
    // Lấy giá từ variant
    BigDecimal unitPrice = cartItem.getVariant().getProduct().getPrice();
    if (cartItem.getVariant().getPriceAdjustment() != null && 
        cartItem.getVariant().getPriceAdjustment().compareTo(BigDecimal.ZERO) != 0) {
      unitPrice = unitPrice.add(cartItem.getVariant().getPriceAdjustment());
    }
    orderItem.setUnitPrice(unitPrice);

    // Snapshot của variant information để đảm bảo data integrity
    Map<String, Object> variantInfo = new HashMap<>();
    if (cartItem.getVariant().getSize() != null) {
      variantInfo.put("sizeName", cartItem.getVariant().getSize().getName());
    }
    if (cartItem.getVariant().getColor() != null) {
      variantInfo.put("colorName", cartItem.getVariant().getColor().getName());
      variantInfo.put("colorHex", cartItem.getVariant().getColor().getHexColor());
    }
    variantInfo.put("productCode", cartItem.getVariant().getProduct().getCode());
    variantInfo.put("productId", cartItem.getVariant().getProduct().getId());
    variantInfo.put("variantId", cartItem.getVariant().getId());
    
    // Thêm thông tin product để đảm bảo hiển thị được ngay cả khi product bị xóa
    variantInfo.put("productName", cartItem.getVariant().getProduct().getName());
    variantInfo.put("productSlug", cartItem.getVariant().getProduct().getSlug());
    variantInfo.put("productPrice", cartItem.getVariant().getProduct().getPrice());
    variantInfo.put("productSalePrice", cartItem.getVariant().getProduct().getSalePrice());
    variantInfo.put("productImages", cartItem.getVariant().getProduct().getImageUrls());
    
    orderItem.setVariantInfo(variantInfo);
    orderItem.setProductName(cartItem.getVariant().getProduct().getName());

    return orderItem;
  }
}
