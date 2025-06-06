package org.longg.nh.kickstyleecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KickstyleEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KickstyleEcommerceApplication.class, args);
    }

}
