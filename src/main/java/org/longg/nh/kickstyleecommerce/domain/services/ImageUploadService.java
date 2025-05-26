package org.longg.nh.kickstyleecommerce.domain.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.eps.shared.models.exceptions.ResponseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.ImageRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.ImageResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.values.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageUploadService {

  @Value("${upload.dir}")
  private String uploadDir;

  @Value("${url.port}")
  private String portServer;

  @Value("${url.host}")
  private String hostServer;

  public ImageResponse uploadImage(ImageRequest request) throws IOException {
    List<File> files = new ArrayList<>();

    if (request.getFile() == null || request.getFile().isEmpty()) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Không tìm thấy file");
    }

    List<String> codes = upLoadToServer(request.getFile());

    for (int i = 0; i < request.getFile().size(); i++) {
      MultipartFile file = request.getFile().get(i);
      if (file != null && !file.isEmpty()) {
        File fileResponse = new File();
        fileResponse.setUrl(generateImageUrl(codes.get(i), file.getOriginalFilename()));
        files.add(fileResponse);
      }
    }

    ImageResponse response = new ImageResponse();
    response.setFile(files);
    return response;
  }

  private List<String> upLoadToServer(List<MultipartFile> files) throws IOException {
    if (files == null || files.isEmpty()) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Không tìm thấy file");
    }

    Path path = Paths.get(uploadDir);
    if (!Files.exists(path)) {
      Files.createDirectories(path);
    }

    List<String> codes = new ArrayList<>();

    for (MultipartFile file : files) {
      if (file != null && !file.isEmpty()) {
        String code = RandomStringUtils.randomAlphanumeric(10);
        String originalFilename = file.getOriginalFilename();
        Path fileSave = path.resolve(code + "-" + Objects.requireNonNull(originalFilename));
        try (InputStream is = file.getInputStream()) {
          Files.copy(is, fileSave, StandardCopyOption.REPLACE_EXISTING);
        }
        codes.add(code);
      }
    }

    return codes;
  }

  private String generateImageUrl(String code, String originalFilename) {
    return hostServer + ":" + portServer + "/kick-style/" + code + "-" + originalFilename;
  }
}
