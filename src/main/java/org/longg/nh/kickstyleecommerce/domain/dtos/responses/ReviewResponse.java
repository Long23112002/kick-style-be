package org.longg.nh.kickstyleecommerce.domain.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Answers;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.User;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReviewResponse {

    private Long id;

    @JsonIgnoreProperties({"reviews", "orders", "answers"})
    private User user;

    @JsonIgnoreProperties({"reviews", "orderItems", "user"})
    private Order order;

    private Integer rating;

    private String comment;

    private List<String> images;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean isDeleted;

    @JsonIgnoreProperties({"review", "user"})
    private List<Answers> answers;
}
