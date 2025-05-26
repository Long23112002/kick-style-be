package org.longg.nh.kickstyleecommerce.infrastructure.config.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRole {
    String[] value();
}


