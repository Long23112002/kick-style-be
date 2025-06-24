package org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Gender;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    private String phone;

    private String address;
    
    private String district;
    
    private String ward;
    
    private Gender gender;
    
    private String avatarUrl;
} 