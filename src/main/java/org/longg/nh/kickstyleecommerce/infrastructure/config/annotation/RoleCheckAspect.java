package org.longg.nh.kickstyleecommerce.infrastructure.config.annotation;

import com.eps.shared.models.exceptions.ResponseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.longg.nh.kickstyleecommerce.domain.services.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

  private final JwtService jwtService;

    @Around("@annotation(checkRole)")
    public Object checkUserRole(ProceedingJoinPoint joinPoint, CheckRole checkRole) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null)
            throw new ResponseException(HttpStatus.UNAUTHORIZED, "Không tìm thấy request attributes");

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseException(
                    HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập hoặc token không hợp lệ");
        }

        String token = authHeader.substring(7);
        String userRole = jwtService.extractRole(token);

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return joinPoint.proceed();
        }

        boolean hasPermission =
                Arrays.stream(checkRole.value())
                        .anyMatch(allowedRole -> allowedRole.equalsIgnoreCase(userRole));

        if (!hasPermission) {
            throw new ResponseException(
                    HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập vào tài nguyên này");
        }

        return joinPoint.proceed();
    }

}
