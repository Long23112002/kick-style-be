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

  List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

  @Query(value = "SELECT last_value + 1 FROM orders.orders_id_seq", nativeQuery = true)
  Long getNextSequence();

  // Thống kê doanh thu theo ngày
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND DATE(o.createdAt) = DATE(:date)")
  BigDecimal getTotalRevenueByDate(
      @Param("paymentStatus") PaymentStatus paymentStatus, @Param("date") Timestamp date);

  // Thống kê doanh thu theo tháng
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
  BigDecimal getTotalRevenueByMonth(
      @Param("paymentStatus") PaymentStatus paymentStatus,
      @Param("year") int year,
      @Param("month") int month);

  // Thống kê doanh thu theo năm
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND YEAR(o.createdAt) = :year")
  BigDecimal getTotalRevenueByYear(
      @Param("paymentStatus") PaymentStatus paymentStatus, @Param("year") int year);

  // Thống kê số đơn hàng theo ngày với điều kiện payment status
  @Query(
      "SELECT COUNT(o) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND DATE(o.createdAt) = DATE(:date)")
  Long getTotalOrdersByDate(
      @Param("paymentStatus") PaymentStatus paymentStatus, @Param("date") Timestamp date);

  // Thống kê số đơn hàng theo ngày
  @Query("SELECT COUNT(o) FROM Order o " + "WHERE DATE(o.createdAt) = DATE(:date)")
  Long getOrderCountByDate(@Param("date") Timestamp date);

  // Thống kê số đơn hàng theo tháng với điều kiện payment status
  @Query(
      "SELECT COUNT(o) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
  Long getTotalOrdersByMonth(
      @Param("paymentStatus") PaymentStatus paymentStatus,
      @Param("year") int year,
      @Param("month") int month);

  // Thống kê số đơn hàng theo tháng
  @Query(
      "SELECT COUNT(o) FROM Order o "
          + "WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
  Long getOrderCountByMonth(@Param("year") int year, @Param("month") int month);

  // Thống kê số đơn hàng theo năm với điều kiện payment status
  @Query(
      "SELECT COUNT(o) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND YEAR(o.createdAt) = :year")
  Long getTotalOrdersByYear(
      @Param("paymentStatus") PaymentStatus paymentStatus, @Param("year") int year);

  // Thống kê số đơn hàng theo năm
  @Query("SELECT COUNT(o) FROM Order o " + "WHERE YEAR(o.createdAt) = :year")
  Long getOrderCountByYear(@Param("year") int year);

  // Doanh thu trong khoảng thời gian
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
          + "WHERE o.paymentStatus = :paymentStatus "
          + "AND o.createdAt BETWEEN :startDate AND :endDate")
  BigDecimal getTotalRevenueBetweenDates(
      @Param("paymentStatus") PaymentStatus paymentStatus,
      @Param("startDate") Timestamp startDate,
      @Param("endDate") Timestamp endDate);
}
