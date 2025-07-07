package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends IBaseRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.variant.id = :variantId")
    Optional<CartItem> findByCartIdAndVariantId(@Param("cartId") Long cartId, @Param("variantId") Long variantId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.variant.id = :variantId")
    List<CartItem> findByVariantId(@Param("variantId") Long variantId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.variant.id IN :variantIds")
    void deleteByCartIdAndVariantIdIn(@Param("cartId") Long cartId, @Param("variantIds") List<Long> variantIds);
    
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.variant.id = :variantId")
    void deleteAllByVariantId(@Param("variantId") Long variantId);
    
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.variant.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
