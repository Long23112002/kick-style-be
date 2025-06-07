package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends IBaseRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    // Thống kê user với tổng số tiền đã mua
    @Query(value = "SELECT u.id as userId, u.full_name as fullName, u.email, " +
                   "COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) as totalSpent, " +
                   "COUNT(CASE WHEN o.payment_status = 'PAID' THEN o.id END) as totalOrders " +
                   "FROM users.user u LEFT JOIN orders.orders o ON u.id = o.user_id " +
                   "WHERE u.is_deleted = false " +
                   "GROUP BY u.id, u.full_name, u.email " +
                   "ORDER BY totalSpent DESC", nativeQuery = true)
    List<Object[]> getUsersWithTotalSpent();
    
    // Thống kê user theo khoảng thời gian
    @Query(value = "SELECT u.id as userId, u.full_name as fullName, u.email, " +
                   "COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) as totalSpent, " +
                   "COUNT(CASE WHEN o.payment_status = 'PAID' THEN o.id END) as totalOrders " +
                   "FROM users.user u LEFT JOIN orders.orders o ON u.id = o.user_id " +
                   "WHERE u.is_deleted = false AND (o.created_at IS NULL OR o.created_at BETWEEN :startDate AND :endDate) " +
                   "GROUP BY u.id, u.full_name, u.email " +
                   "ORDER BY totalSpent DESC", nativeQuery = true)
    List<Object[]> getUsersWithTotalSpentBetweenDates(@Param("startDate") Timestamp startDate, 
                                                      @Param("endDate") Timestamp endDate);
    
    // Lấy top customer chi tiêu nhiều nhất (sử dụng native query để có LIMIT)
    @Query(value = "SELECT u.id as userId, u.full_name as fullName, u.email, " +
                   "COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) as totalSpent, " +
                   "COUNT(CASE WHEN o.payment_status = 'PAID' THEN o.id END) as totalOrders " +
                   "FROM users.user u LEFT JOIN orders.orders o ON u.id = o.user_id " +
                   "WHERE u.is_deleted = false " +
                   "GROUP BY u.id, u.full_name, u.email " +
                   "HAVING COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) > 0 " +
                   "ORDER BY COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopCustomers(@Param("limit") int limit);
}
