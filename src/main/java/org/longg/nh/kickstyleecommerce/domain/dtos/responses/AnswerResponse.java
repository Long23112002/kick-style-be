package org.longg.nh.kickstyleecommerce.domain.dtos.responses;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.entities.User;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {

  private Long id;

  private User user;

  private List<String> images;

  private String answer;

  private Review review;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Boolean isDeleted;
}
