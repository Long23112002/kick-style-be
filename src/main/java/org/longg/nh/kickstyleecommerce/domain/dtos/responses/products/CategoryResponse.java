package org.longg.nh.kickstyleecommerce.domain.dtos.responses.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {

  private Long id;

  private String name;

  private String slug;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Boolean isDeleted;
}
