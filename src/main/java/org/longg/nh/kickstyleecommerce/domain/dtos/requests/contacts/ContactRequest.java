package org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String message;

    @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT", message = "Mức độ ưu tiên không hợp lệ")
    private String priority = "NORMAL";
} 