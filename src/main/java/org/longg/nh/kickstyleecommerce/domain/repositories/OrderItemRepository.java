package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderItemRepository extends IBaseRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    // Thống kê sản phẩm bán chạy theo ngày
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND DATE(oi.createdAt) = DATE(:date) " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByDate(@Param("date") Timestamp date);
    
    // Thống kê sản phẩm bán chạy theo tháng
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND YEAR(oi.createdAt) = :year AND MONTH(oi.createdAt) = :month " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByMonth(@Param("year") int year, @Param("month") int month);
    
    // Thống kê sản phẩm bán chạy theo năm
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND YEAR(oi.createdAt) = :year " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByYear(@Param("year") int year);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo ngày
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND DATE(oi.createdAt) = DATE(:date)")
    Long getTotalProductsSoldByDate(@Param("date") Timestamp date);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo tháng
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND YEAR(oi.createdAt) = :year AND MONTH(oi.createdAt) = :month")
    Long getTotalProductsSoldByMonth(@Param("year") int year, @Param("month") int month);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo năm
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND YEAR(oi.createdAt) = :year")
    Long getTotalProductsSoldByYear(@Param("year") int year);
    
    // Thống kê doanh thu theo sản phẩm trong khoảng thời gian
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND oi.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getProductRevenueStats(@Param("startDate") Timestamp startDate, 
                                          @Param("endDate") Timestamp endDate);
} 