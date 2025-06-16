package org.longg.nh.kickstyleecommerce.app.api;

import org.longg.nh.kickstyleecommerce.domain.services.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/v1/vnpay")
public class VNPayController {

  @Autowired private VNPayService vnPayService;

  @PostMapping("/create-payment")
  public String createPayment(
      @RequestParam("amount") long amountRequest, @RequestParam("orderId") long orderId)
      throws UnsupportedEncodingException {
    return vnPayService.createPayment(amountRequest, orderId);
  }

  @GetMapping("/payment-info")
  public ResponseEntity<?> paymentSuccess(
      @RequestParam("status") String status, @RequestParam("orderId") Long orderId) {
    return vnPayService.paymentSuccess(status, orderId);
  }
}
