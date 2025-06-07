package org.longg.nh.kickstyleecommerce.domain.services.contacts;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts.ContactReplyRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts.ContactRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.contacts.ContactReplyResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.contacts.ContactResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Contact;
import org.longg.nh.kickstyleecommerce.domain.entities.ContactReply;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.ContactStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.ContactPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ContactReplyRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ContactRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.longg.nh.kickstyleecommerce.domain.services.MailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService implements IBaseService<Contact, Long, ContactResponse, ContactRequest, ContactResponse> {

    private final ContactPersistence contactPersistence;
    private final ContactRepository contactRepository;
    private final ContactReplyRepository contactReplyRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    public IBasePersistence<Contact, Long> getPersistence() {
        return contactPersistence;
    }

    @Transactional
    public ContactResponse submitContact(ContactRequest request) {
        Contact contact = Contact.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactStatus.PENDING)
                .priority(request.getPriority())
                .build();

        contact = contactRepository.save(contact);
        
        // Send confirmation email
        try {
            sendContactConfirmationEmail(contact);
        } catch (Exception e) {
            log.error("Failed to send contact confirmation email", e);
        }

        return mapToContactResponse(contact);
    }

    @Transactional
    public ContactResponse replyToContact(Long contactId, ContactReplyRequest request, Long adminId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "Liên hệ không tồn tại"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "Admin không tồn tại"));

        ContactReply reply = ContactReply.builder()
                .contact(contact)
                .admin(admin)
                .replyMessage(request.getReplyMessage())
                .isEmailSent(false)
                .build();

        reply = contactReplyRepository.save(reply);

        // Update contact status
        contact.setStatus(request.getNewStatus());
        if (request.getNewStatus() == ContactStatus.RESOLVED || 
            request.getNewStatus() == ContactStatus.CLOSED) {
            contact.setResolvedAt(new Timestamp(System.currentTimeMillis()));
        }
        contact = contactRepository.save(contact);

        // Send email if requested
        if (request.getSendEmail() != null && request.getSendEmail()) {
            try {
                sendReplyEmail(contact, reply);
                reply.setIsEmailSent(true);
                reply.setEmailSentAt(new Timestamp(System.currentTimeMillis()));
                contactReplyRepository.save(reply);
            } catch (Exception e) {
                log.error("Failed to send reply email", e);
            }
        }

        return mapToContactResponse(contact);
    }

    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable).map(this::mapToContactResponse);
    }

    public Page<ContactResponse> searchContacts(ContactStatus status, String priority, 
                                              Long assignedTo, String email, 
                                              String fullName, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        return contactRepository.findContactsWithFilters(statusStr, priority, assignedTo, 
                                                        email, fullName, pageable)
                .map(this::mapToContactResponse);
    }

    @Transactional
    public ContactResponse assignContact(Long contactId, Long adminId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "Liên hệ không tồn tại"));

        contact.setAssignedTo(adminId);
        if (contact.getStatus() == ContactStatus.PENDING) {
            contact.setStatus(ContactStatus.IN_PROGRESS);
        }
        
        contact = contactRepository.save(contact);
        return mapToContactResponse(contact);
    }

    public Page<ContactResponse> getContactsByStatus(ContactStatus status, Pageable pageable) {
        return contactRepository.findByStatus(status, pageable)
                .map(this::mapToContactResponse);
    }

    public Page<ContactResponse> getContactHistory(String email, Pageable pageable) {
        return contactRepository.findContactsWithFilters(null, null, null, email, null, pageable)
                .map(this::mapToContactResponse);
    }

    public Page<ContactResponse> getUnassignedContacts(Pageable pageable) {
        return contactRepository.findUnassignedContacts(pageable)
                .map(this::mapToContactResponse);
    }

    private void sendContactConfirmationEmail(Contact contact) {
        String subject = "Xác nhận đã nhận liên hệ - KickStyle";
        String message = String.format(
                "Xin chào %s,\n\nChúng tôi đã nhận được liên hệ của bạn và sẽ phản hồi sớm nhất.\n\nTrân trọng,\nĐội ngũ KickStyle",
                contact.getFullName()
        );
        mailService.sendSimpleMessage(contact.getEmail(), subject, message);
    }

    private void sendReplyEmail(Contact contact, ContactReply reply) {
        String subject = "Phản hồi từ KickStyle";
        String message = String.format(
                "Xin chào %s,\n\nPhản hồi: %s\n\nTrân trọng,\nĐội ngũ KickStyle",
                contact.getFullName(), reply.getReplyMessage()
        );
        mailService.sendSimpleMessage(contact.getEmail(), subject, message);
    }

    private ContactResponse mapToContactResponse(Contact contact) {
        List<ContactReplyResponse> replies = contactReplyRepository.findByContactIdOrderByCreatedAtAsc(contact.getId())
                .stream()
                .map(this::mapToContactReplyResponse)
                .collect(Collectors.toList());

        String assignedToName = null;
        if (contact.getAssignedTo() != null) {
            User admin = userRepository.findById(contact.getAssignedTo()).orElse(null);
            if (admin != null) {
                assignedToName = admin.getFullName();
            }
        }

        return ContactResponse.builder()
                .id(contact.getId())
                .fullName(contact.getFullName())
                .email(contact.getEmail())
                .phoneNumber(contact.getPhoneNumber())
                .address(contact.getAddress())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .status(contact.getStatus())
                .priority(contact.getPriority())
                .assignedTo(contact.getAssignedTo())
                .assignedToName(assignedToName)
                .resolvedAt(contact.getResolvedAt())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .replies(replies)
                .build();
    }

    private ContactReplyResponse mapToContactReplyResponse(ContactReply reply) {
        return ContactReplyResponse.builder()
                .id(reply.getId())
                .contactId(reply.getContact().getId())
                .adminId(reply.getAdmin().getId())
                .adminName(reply.getAdmin().getFullName())
                .replyMessage(reply.getReplyMessage())
                .isEmailSent(reply.getIsEmailSent())
                .emailSentAt(reply.getEmailSentAt())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    public void deleteById(HeaderContext context, Long id) {
        contactRepository.deleteById(id);
    }

    private BiFunction<HeaderContext, Contact, ContactResponse> mappingResponseHandler() {
        return (context, contact) -> mapToContactResponse(contact);
    }
} 