package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.models.exceptions.ResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.VnpayTransaction;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.repositories.OrderRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.VnpayTransactionRepository;
import org.longg.nh.kickstyleecommerce.domain.services.orders.OrderService;
import org.longg.nh.kickstyleecommerce.infrastructure.config.VNPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

  @Autowired private VNPayConfig vnPayConfig;

  @Autowired private HttpServletRequest request;

  @Autowired private OrderRepository orderRepository;

  @Autowired private VnpayTransactionRepository vnpayTransactionRepository;

  @Value("${vnpay.url}")
  private String vnPayUrl;

  @Value("${vnpay.return.url}")
  private String returnUrl;

  @Value("${vnpay.tmn.code}")
  private String tmnCode;

  @Value("${vnpay.secret.key}")
  private String secretKey;

  @Value("${vnpay.api.url}")
  private String vnPayAPIUrl;

  @Value("${vnpay.version}")
  private String vnPayVersion;

  @Value("${vnpay.command}")
  private String command;

  public String createPayment(long amountRequest, long orderId) {
    Map<String, String> vnpParams = mappingParams(request, amountRequest, orderId);
    String secureHash = encode(request, amountRequest, orderId);

    String queryUrl = buildQueryUrl(vnpParams);
    queryUrl += "&vnp_SecureHash=" + secureHash;

    return vnPayUrl + "?" + queryUrl;
  }

  public Page<VnpayTransaction> filter(Long userId, Pageable pageable) {
    return vnpayTransactionRepository.filter(userId, pageable);
  }

  public ResponseEntity<?> paymentSuccess(String status, Long orderId, String url) {
    Map<String, String> vnpParams = extractVnpParams(url);

    if ("00".equals(status)) {
      Order order =
          orderRepository
              .findById(orderId)
              .orElseThrow(
                  () ->
                      new ResponseException(
                          HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

      order.setPaymentStatus(PaymentStatus.PAID);
      orderRepository.save(order);

      VnpayTransaction transaction = new VnpayTransaction();
      transaction.setTransactionCode(vnpParams.get("vnp_TransactionNo"));
      transaction.setBankCode(vnpParams.get("vnp_BankCode"));
      transaction.setPaymentMethod(vnpParams.get("vnp_PayMethod"));
      transaction.setCardType(vnpParams.get("vnp_CardType"));
      transaction.setAmount(
          new BigDecimal(vnpParams.get("vnp_Amount")).divide(BigDecimal.valueOf(100)));
      transaction.setCurrency(vnpParams.get("vnp_CurrCode"));
      transaction.setStatus(status);
      transaction.setOrderInfo(vnpParams.get("vnp_OrderInfo"));
      transaction.setPayDate(vnpParams.get("vnp_PayDate"));
      transaction.setResponseCode(vnpParams.get("vnp_ResponseCode"));
      transaction.setTmnCode(vnpParams.get("vnp_TmnCode"));
      transaction.setSecureHash(vnpParams.get("vnp_SecureHash"));
      transaction.setOrder(order);

      vnpayTransactionRepository.save(transaction);

      return ResponseEntity.ok("redirect:/success");
    } else {
      return ResponseEntity.badRequest().body("Thanh toán thất bại");
    }
  }

  public boolean match(Order order, String secure) {
    String orderSecure = encode(order.getId());
    return orderSecure.equals(secure);
  }

  public String encode(HttpServletRequest request, long amountRequest, long orderId) {
    Map<String, String> vnpParams = mappingParams(request, amountRequest, orderId);
    return generateSecureHash(vnpParams);
  }

  public String encode(long orderId) {
    Map<String, String> encodeMap = new HashMap<>();
    encodeMap.put("orderId", orderId + "");
    return generateSecureHash(encodeMap);
  }

  private Map<String, String> mappingParams(
      HttpServletRequest request, long amountRequest, long orderId) {
    String orderType = "other";
    long amount = calculateAmount(amountRequest);

    String vnpTxnRef = orderId + "";
    String vnpIpAddress = vnPayConfig.getIpAddress(request);
    String vnpTmnCode = tmnCode;

    Map<String, String> vnpParams =
        buildParamVNPay(vnpTxnRef, amount, orderType, vnpIpAddress, vnpTmnCode);
    return vnpParams;
  }

  public Map<String, String> extractVnpParams(String url) {
    Map<String, String> params = new HashMap<>();
    try {
      String[] parts = url.split("\\?");
      if (parts.length < 2) return params;
      String query = parts[1];
      for (String param : query.split("&")) {
        String[] keyValue = param.split("=");
        if (keyValue.length == 2) {
          params.put(
              URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
              URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return params;
  }

  private Map<String, String> buildParamVNPay(
      String vnpTxnRef, long amount, String orderType, String vnpIpAddress, String vnpTmnCode) {
    Map<String, String> vnpParams = new HashMap<>();

    Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    String vnpCreateDate = format.format(cld.getTime());

    vnpParams.put("vnp_CreateDate", vnpCreateDate);
    vnpParams.put("vnp_Version", vnPayVersion);
    vnpParams.put("vnp_Command", command);
    vnpParams.put("vnp_TmnCode", vnpTmnCode);
    vnpParams.put("vnp_Amount", String.valueOf(amount));
    vnpParams.put("vnp_CurrCode", "VND");
    //        vnpParams.put("vnp_BankCode", "NCB");
    vnpParams.put("vnp_TxnRef", vnpTxnRef);
    vnpParams.put("vnp_OrderInfo", encode(Long.valueOf(vnpTxnRef)));
    vnpParams.put("vnp_OrderType", orderType);
    vnpParams.put("vnp_Locale", "vn");
    vnpParams.put("vnp_ReturnUrl", returnUrl);
    vnpParams.put("vnp_IpAddr", vnpIpAddress);
    return vnpParams;
  }

  private String buildQueryUrl(Map<String, String> vnpParams) {
    List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
    Collections.sort(fieldNames);

    StringBuilder query = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();

    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = vnpParams.get(fieldName);

      if (fieldValue != null && !fieldValue.isEmpty()) {
        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
        query.append('=');
        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

        if (itr.hasNext()) {
          query.append('&');
        }
      }
    }

    return query.toString();
  }

  private String generateSecureHash(Map<String, String> vnpParams) {
    List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
    Collections.sort(fieldNames);

    StringBuilder hashData = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();

    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = vnpParams.get(fieldName);

      if (fieldValue != null && !fieldValue.isEmpty()) {
        hashData.append(fieldName);
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

        if (itr.hasNext()) {
          hashData.append('&');
        }
      }
    }

    return vnPayConfig.hmacSHA512(secretKey, hashData.toString());
  }

  private long calculateAmount(long amountRequest) {
    return amountRequest * 100;
  }
}
