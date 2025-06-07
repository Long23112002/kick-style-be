package org.longg.nh.kickstyleecommerce.domain.dtos.responses.contacts;

import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactReplyResponse {

    private Long id;
    private Long contactId;
    private Long adminId;
    private String adminName; // Tên admin phản hồi
    private String replyMessage;
    private Boolean isEmailSent;
    private Timestamp emailSentAt;
    private Timestamp createdAt;
} 