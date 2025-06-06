package org.longg.nh.kickstyleecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KickStyle E-commerce API")
                        .version("1.0.0")
                        .description("Tài liệu API cho nền tảng thương mại điện tử KickStyle - " +
                                    "Hệ thống quản lý toàn diện cho sản phẩm, đơn hàng, người dùng, mã giảm giá và thanh toán")
                        .contact(new Contact()
                                .name("Đội phát triển KickStyle")
                                .email("dev@kickstyle.com")
                                .url("https://kickstyle.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Máy chủ phát triển"),
                        new Server()
                                .url("https://api.kickstyle.com")
                                .description("Máy chủ sản xuất")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Xác thực JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 