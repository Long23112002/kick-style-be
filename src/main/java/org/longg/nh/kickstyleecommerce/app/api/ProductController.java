package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ProductResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.longg.nh.kickstyleecommerce.domain.services.products.ProductService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController
    implements IBaseApi<Product, Long, ProductResponse, ProductRequest, ProductResponse> {

  private final ProductService productService;

  @Override
  public IBaseService<Product, Long, ProductResponse, ProductRequest, ProductResponse>
      getService() {
    return productService;
  }
}
