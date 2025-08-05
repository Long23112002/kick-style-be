package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.dtos.OrderParam;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends IBaseRepository<Order, Long> {

  @Query(
      """
    SELECT o FROM Order o
    WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :#{#param.search}, '%'))
       OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :#{#param.search}, '%'))
       OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :#{#param.search}, '%'))
       OR LOWER(o.code) LIKE LOWER(CONCAT('%', :#{#param.search}, '%'))
    """)
  Page<Order> filter(OrderParam param, Pageable pageable);

  Optional<Order> findByCode(String code);

  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);


  @Query(value = "SELECT last_value + 1 FROM orders.orders_id_seq", nativeQuery = true)
  Long getNextSequence();

  // Doanh thu theo ngày
  @Query(
          "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND DATE(o.updatedAt) = DATE(:date)")
  BigDecimal getTotalRevenueByDate(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("date") Timestamp date);

  // Doanh thu theo tháng
  @Query(
          "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND YEAR(o.updatedAt) = :year AND MONTH(o.createdAt) = :month")
  BigDecimal getTotalRevenueByMonth(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("year") int year,
          @Param("month") int month);

  // Doanh thu theo năm
  @Query(
          "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND YEAR(o.updatedAt) = :year")
  BigDecimal getTotalRevenueByYear(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("year") int year);

  // Số đơn hàng theo ngày
  @Query(
          "SELECT COUNT(o) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND DATE(o.updatedAt) = DATE(:date)")
  Long getTotalOrdersByDate(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("date") Timestamp date);

  // Số đơn hàng theo ngày (không theo status)
  @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = DATE(:date)")
  Long getOrderCountByDate(@Param("date") Timestamp date);

  // Số đơn hàng theo tháng
  @Query(
          "SELECT COUNT(o) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND YEAR(o.updatedAt) = :year AND MONTH(o.updatedAt) = :month")
  Long getTotalOrdersByMonth(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("year") int year,
          @Param("month") int month);

  // Số đơn hàng theo năm
  @Query(
          "SELECT COUNT(o) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND YEAR(o.updatedAt) = :year")
  Long getTotalOrdersByYear(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("year") int year);

  // Doanh thu theo khoảng thời gian
  @Query(
          "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND o.updatedAt BETWEEN :startDate AND :endDate")
  BigDecimal getTotalRevenueByDateRange(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("startDate") Timestamp startDate,
          @Param("endDate") Timestamp endDate);

  // Số đơn hàng theo khoảng thời gian
  @Query(
          "SELECT COUNT(o) FROM Order o "
                  + "WHERE o.status IN :orderStatuses "
                  + "AND o.updatedAt BETWEEN :startDate AND :endDate")
  Long getTotalOrdersByDateRange(
          @Param("orderStatuses") List<OrderStatus> orderStatuses,
          @Param("startDate") Timestamp startDate,
          @Param("endDate") Timestamp endDate);

}
