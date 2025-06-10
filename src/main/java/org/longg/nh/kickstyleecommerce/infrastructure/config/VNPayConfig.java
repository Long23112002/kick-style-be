package org.longg.nh.kickstyleecommerce.infrastructure.config;

import com.eps.shared.models.exceptions.ResponseException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class VNPayConfig {

  public String hmacSHA512(String key, String data) {
    try {
      if (key == null || data == null) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Invalid key or data");
      }

      Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
      byte[] keyBytes = key.getBytes();
      SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA512");
      hmacSHA512.init(secretKeySpec);
      byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
      byte[] result = hmacSHA512.doFinal(dataBytes);

      StringBuilder sb = new StringBuilder(2 * result.length);
      for (byte b : result) {
        sb.append(String.format("%02x", b & 0xff));
      }

      return sb.toString();
    } catch (Exception e) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Invalid key or data");
    }
  }

  public String getIpAddress(HttpServletRequest request) {
    String ipAddress;

    try {
      ipAddress = request.getHeader("X-FORWARDED-FOR");
      if (ipAddress == null) {
        ipAddress = request.getRemoteAddr();
      }
    } catch (Exception e) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Invalid IP Address");
    }

    return ipAddress;
  }

  public String getRandomNumber(int length) {
    Random random = new Random();
    String numbers = "0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(numbers.charAt(random.nextInt(numbers.length())));
    }
    return sb.toString();
  }
}
