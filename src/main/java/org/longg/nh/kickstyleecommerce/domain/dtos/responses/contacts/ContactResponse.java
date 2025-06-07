package org.longg.nh.kickstyleecommerce.domain.dtos.responses.contacts;

import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.ContactStatus;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String subject;
    private String message;
    private ContactStatus status;
    private String priority;
    private Long assignedTo;
    private String assignedToName; // Tên admin được phân công
    private Timestamp resolvedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<ContactReplyResponse> replies;
} 