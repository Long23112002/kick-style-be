package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ProductResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.longg.nh.kickstyleecommerce.domain.services.products.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/detail/{id}")
  public ProductResponse getProductById(@PathVariable Long id) {
    return productService.findById(id);
  }
  
  @GetMapping("/orders/{id}")
  @Operation(summary = "Lấy thông tin sản phẩm cho đơn hàng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thông tin sản phẩm thành công (kể cả khi sản phẩm đã bị xóa)"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm (cả khi đã xóa hoặc chưa từng tồn tại)")
  })
  public ProductResponse getProductForOrderById(@PathVariable Long id) {
    return productService.findProductResponseForOrderById(id);
  }

  @PutMapping("/variants/{variantId}/toggle")
  @Operation(summary = "Bật/tắt variant sản phẩm")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái variant thành công"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy variant")
  })
  public ProductResponse toggleVariantStatus(@PathVariable Long variantId, @RequestParam Boolean enabled) {
    return productService.toggleVariantStatus(variantId, enabled);
  }
}
