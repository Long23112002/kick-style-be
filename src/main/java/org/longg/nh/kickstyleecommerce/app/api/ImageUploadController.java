package org.longg.nh.kickstyleecommerce.app.api;

import org.longg.nh.kickstyleecommerce.domain.dtos.requests.ImageRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.ImageResponse;
import org.longg.nh.kickstyleecommerce.domain.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/images")
public class ImageUploadController {

  @Autowired private ImageUploadService imageUploadService;

  @PostMapping
  public ResponseEntity<ImageResponse> uploadImage(@ModelAttribute ImageRequest request)
      throws IOException {
    return ResponseEntity.ok(imageUploadService.uploadImage(request));
  }
}
