package org.longg.nh.kickstyleecommerce.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.User;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

  private Long id;

  private User user;

  private Order order;

  private Integer rating;

  private String comment;

  private List<String> images;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Boolean isDeleted;
}
