package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Contact;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ContactRepository extends IBaseRepository<Contact, Long> {
    
    // Tìm kiếm contact theo status
    Page<Contact> findByStatus(ContactStatus status, Pageable pageable);
    
    // Tìm kiếm contact được giao cho admin
    Page<Contact> findByAssignedTo(Long adminId, Pageable pageable);
    
    // Tìm kiếm contact theo email
    List<Contact> findByEmailOrderByCreatedAtDesc(String email);
    
    // Tìm kiếm contact theo email với phân trang
    Page<Contact> findByEmailOrderByCreatedAtDesc(String email, Pageable pageable);
    
    // Tìm kiếm với filter - sử dụng native query
    @Query(value = "SELECT * FROM contacts.contacts c WHERE " +
           "c.is_deleted = false AND " +
           "(:status IS NULL OR c.status = CAST(:status AS VARCHAR)) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:assignedTo IS NULL OR c.assigned_to = :assignedTo) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:fullName IS NULL OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')))", 
           nativeQuery = true)
    Page<Contact> findContactsWithFilters(@Param("status") String status,
                                         @Param("priority") String priority,
                                         @Param("assignedTo") Long assignedTo,
                                         @Param("email") String email,
                                         @Param("fullName") String fullName,
                                         Pageable pageable);
    
    // Thống kê contact theo status
    @Query("SELECT c.status, COUNT(c) FROM Contact c GROUP BY c.status")
    List<Object[]> getContactStatsByStatus();
    
    // Thống kê contact trong khoảng thời gian
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Long countContactsBetweenDates(@Param("startDate") Timestamp startDate, 
                                   @Param("endDate") Timestamp endDate);
    
    // Lấy contact chưa được phân công
    @Query("SELECT c FROM Contact c WHERE c.assignedTo IS NULL AND c.status = 'PENDING'")
    Page<Contact> findUnassignedContacts(Pageable pageable);
} 