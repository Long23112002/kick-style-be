package org.longg.nh.kickstyleecommerce.domain.dtos.responses.products;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {

  private Long id;

  private String name;

  private String league;

  private String country;

  private String logoUrl;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Boolean isDeleted;
}
