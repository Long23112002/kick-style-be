package org.longg.nh.kickstyleecommerce.domain.dtos.requests;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {

  @NotNull(message = "Người dùng không được để trống")
  private Long userId;

  private List<String> images;

  @NotNull(message = "Câu trả lời không được để trống")
  private String answer;

  @NotNull(message = "Đánh giá không được để trống")
  private Long reviewId;
}
