package org.longg.nh.kickstyleecommerce.domain.services.payments;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.payments.PaymentMethodRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.payments.PaymentMethodResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.PaymentMethod;
import org.longg.nh.kickstyleecommerce.domain.persistence.PaymentMethodPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.PaymentMethodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService implements IBaseService<PaymentMethod, Long, PaymentMethodResponse, PaymentMethodRequest, PaymentMethodResponse> {

  private final PaymentMethodPersistence paymentMethodPersistence;
  private final PaymentMethodRepository paymentMethodRepository;

  @Override
  public IBasePersistence<PaymentMethod, Long> getPersistence() {
    return paymentMethodPersistence;
  }

  public Page<PaymentMethod> filter(Pageable pageable){
    return paymentMethodRepository.findAll(pageable);
  }

  public PaymentMethodResponse createPaymentMethod(HeaderContext context, PaymentMethodRequest request) {
    // Kiểm tra name không trùng
    if (paymentMethodRepository.existsByName(request.getName())) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Tên phương thức thanh toán đã tồn tại");
    }

    PaymentMethod paymentMethod = PaymentMethod.builder()
        .name(request.getName())
        .description(request.getDescription())
        .isActive(true)
        .build();

    paymentMethod = paymentMethodRepository.save(paymentMethod);
    return mapToPaymentMethodResponse(paymentMethod);
  }

  public PaymentMethodResponse updatePaymentMethod(Long id, PaymentMethodRequest request) {
    PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

    // Kiểm tra name không trùng (trừ chính nó)
    if (!paymentMethod.getName().equals(request.getName()) && 
        paymentMethodRepository.existsByName(request.getName())) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Tên phương thức thanh toán đã tồn tại");
    }

    paymentMethod.setName(request.getName());
    paymentMethod.setDescription(request.getDescription());

    paymentMethod = paymentMethodRepository.save(paymentMethod);
    return mapToPaymentMethodResponse(paymentMethod);
  }

  public List<PaymentMethodResponse> getActivePaymentMethods() {
    return paymentMethodRepository.findByIsActiveTrue()
        .stream()
        .map(this::mapToPaymentMethodResponse)
        .collect(Collectors.toList());
  }

  public List<PaymentMethodResponse> getAllPaymentMethods() {
    return paymentMethodRepository.findAll()
        .stream()
        .map(this::mapToPaymentMethodResponse)
        .collect(Collectors.toList());
  }

  public PaymentMethodResponse activatePaymentMethod(Long id) {
    PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

    paymentMethod.setIsActive(true);
    paymentMethod = paymentMethodRepository.save(paymentMethod);
    
    return mapToPaymentMethodResponse(paymentMethod);
  }

  public PaymentMethodResponse deactivatePaymentMethod(Long id) {
    PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

    paymentMethod.setIsActive(false);
    paymentMethod = paymentMethodRepository.save(paymentMethod);
    
    return mapToPaymentMethodResponse(paymentMethod);
  }

  public void deletePaymentMethod(Long id) {
    PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại"));

    // Kiểm tra có đơn hàng nào đang sử dụng không
    if (paymentMethodRepository.hasActiveOrders(id)) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, 
          "Không thể xóa phương thức thanh toán đang được sử dụng trong đơn hàng");
    }

    paymentMethodRepository.delete(paymentMethod);
  }

  public PaymentMethodResponse mapToPaymentMethodResponse(PaymentMethod paymentMethod) {
    return PaymentMethodResponse.builder()
        .id(paymentMethod.getId())
        .name(paymentMethod.getName())
        .description(paymentMethod.getDescription())
        .isActive(paymentMethod.getIsActive())
        .createdAt(paymentMethod.getCreatedAt())
        .updatedAt(paymentMethod.getUpdatedAt())
        .build();
  }

  private BiFunction<HeaderContext, PaymentMethod, PaymentMethodResponse> mappingResponseHandler() {
    return (context, paymentMethod) -> mapToPaymentMethodResponse(paymentMethod);
  }
} 