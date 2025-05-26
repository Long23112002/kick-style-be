package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.AccessToken;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends IBaseRepository<AccessToken, Long> {
    
    Optional<AccessToken> findByTokenAndRevokedFalse(String token);
    
    List<AccessToken> findByUserAndRevokedFalse(User user);
    
    @Modifying
    @Query("UPDATE AccessToken at SET at.revoked = true WHERE at.user = :user")
    void revokeAllUserTokens(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE AccessToken at SET at.revoked = true WHERE at.token = :token")
    void revokeToken(@Param("token") String token);
} 