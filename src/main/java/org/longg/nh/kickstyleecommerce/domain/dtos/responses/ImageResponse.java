package org.longg.nh.kickstyleecommerce.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.values.File;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse {

  private List<File> file;
}
