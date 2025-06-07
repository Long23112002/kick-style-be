package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.sql.Timestamp;

@Entity
@Table(name = "contact_replies", schema = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User admin; // Admin user who replied

    @Column(name = "reply_message", nullable = false, columnDefinition = "TEXT")
    private String replyMessage;

    @Column(name = "is_email_sent")
    @Builder.Default
    private Boolean isEmailSent = false;

    @Column(name = "email_sent_at")
    private Timestamp emailSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;
} 