package org.longg.nh.kickstyleecommerce.domain.repositories;

import org.longg.nh.kickstyleecommerce.domain.entities.VnpayTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VnpayTransactionRepository extends JpaRepository<VnpayTransaction, Long> {

  @Query("SELECT v FROM VnpayTransaction v WHERE v.order.user.id = :userId")
  Page<VnpayTransaction> filter(Long userId, Pageable pageable);
}
