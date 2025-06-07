package org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts;

import jakarta.validation.constraints.*;
import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.ContactStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(max = 5000, message = "Nội dung phản hồi không được vượt quá 5000 ký tự")
    private String replyMessage;

    @NotNull(message = "Trạng thái liên hệ không được để trống")
    private ContactStatus newStatus;

    private Boolean sendEmail = true; // Có gửi email đến khách hàng không
} 