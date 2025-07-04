package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.CategoryRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.CategoryResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Category;
import org.longg.nh.kickstyleecommerce.domain.services.products.CategoryService;
import org.longg.nh.kickstyleecommerce.infrastructure.config.annotation.CheckRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
// @RequiredArgsConstructor
public class CategoryController
    implements IBaseApi<Category, Long, CategoryResponse, CategoryRequest, CategoryResponse> {

  @Autowired private CategoryService categoryService;

  @Override
  public IBaseService<Category, Long, CategoryResponse, CategoryRequest, CategoryResponse>
      getService() {
    return categoryService;
  }

  @Override
  public ResponseEntity<CategoryResponse> create(
      HeaderContext context, Map<String, Object> headers, CategoryRequest request) {
    System.out.println("CategoryController.create called with request: " + request);
    return IBaseApi.super.create(context, headers, request);
  }

  @Override
  public ResponseEntity<CategoryResponse> update(
      HeaderContext context, Map<String, Object> headers, Long aLong, CategoryRequest request) {
    return IBaseApi.super.update(context, headers, aLong, request);
  }

  @Override
  public ResponseEntity<?> delete(HeaderContext context, Map<String, Object> headers, Long aLong) {
    return IBaseApi.super.delete(context, headers, aLong);
  }
}
