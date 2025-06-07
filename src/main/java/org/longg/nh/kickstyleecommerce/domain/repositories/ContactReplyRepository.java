package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.ContactReply;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactReplyRepository extends IBaseRepository<ContactReply, Long> {
    
    // Lấy tất cả reply của một contact
    List<ContactReply> findByContactIdOrderByCreatedAtAsc(Long contactId);
    
    // Lấy reply của một admin
    List<ContactReply> findByAdminIdOrderByCreatedAtDesc(Long adminId);
    
    // Đếm số reply chưa gửi email
    @Query("SELECT COUNT(cr) FROM ContactReply cr WHERE cr.isEmailSent = false")
    Long countPendingEmailReplies();
    
    // Lấy reply chưa gửi email
    @Query("SELECT cr FROM ContactReply cr WHERE cr.isEmailSent = false ORDER BY cr.createdAt ASC")
    List<ContactReply> findRepliesWithPendingEmails();
} 