package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.OrderItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderItemRepository extends IBaseRepository<OrderItem, Long> {
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId ORDER BY oi.id DESC")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * Tìm order items theo order ID, đảm bảo kết quả bao gồm các variants của sản phẩm đã bị xóa mềm
     * Hữu ích cho việc hiển thị đơn hàng với thông tin sản phẩm đầy đủ
     */
    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.variant WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithFetch(@Param("orderId") Long orderId);
    
    /**
     * Native query để lấy order items theo order ID, bỏ qua tất cả các @Where filters
     * Đảm bảo lấy được đầy đủ thông tin kể cả khi có soft-deleted products/variants
     */
    @Query(value = "SELECT oi.* FROM orders.order_items oi WHERE oi.order_id = :orderId ORDER BY oi.id DESC", 
           nativeQuery = true)
    List<OrderItem> findByOrderIdNative(@Param("orderId") Long orderId);
    
    @Modifying
    @Query("DELETE FROM OrderItem o WHERE o.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
    
    // Thống kê sản phẩm bán chạy theo ngày
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND DATE(oi.createdAt) = DATE(:date) " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByDate(@Param("date") Timestamp date);
    
    // Thống kê sản phẩm bán chạy theo tháng
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND YEAR(oi.createdAt) = :year AND MONTH(oi.createdAt) = :month " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByMonth(@Param("year") int year, @Param("month") int month);
    
    // Thống kê sản phẩm bán chạy theo năm
    @Query("SELECT oi.variant.id as variantId, oi.productName, " +
           "SUM(oi.quantity) as totalSold, " +
           "SUM(oi.quantity * oi.unitPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND YEAR(oi.createdAt) = :year " +
           "GROUP BY oi.variant.id, oi.productName " +
           "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByYear(@Param("year") int year);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo ngày
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND DATE(oi.createdAt) = DATE(:date)")
    Long getTotalProductsSoldByDate(@Param("date") Timestamp date);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo tháng
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND YEAR(oi.createdAt) = :year AND MONTH(oi.createdAt) = :month")
    Long getTotalProductsSoldByMonth(@Param("year") int year, @Param("month") int month);
    
    // Thống kê tổng số lượng sản phẩm đã bán theo năm
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND YEAR(oi.createdAt) = :year")
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