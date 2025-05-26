package org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {

    private Long id;

    private String name;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean isDeleted;
}
