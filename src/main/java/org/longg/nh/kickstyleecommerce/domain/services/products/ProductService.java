package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.utils.functions.PentaConsumer;
import com.eps.shared.utils.functions.QuadConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ProductResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.persistence.ProductPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.longg.nh.kickstyleecommerce.domain.utils.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService
    implements IBaseService<Product, Long, ProductResponse, ProductRequest, ProductResponse> {

  private final ProductPersistence productPersistence;
  private final ProductRepository productRepository;
  private final CategoryService categoryService;
  private final TeamService teamService;
  private final MaterialService materialService;
  private final ProductVariantRepository productVariantRepository;

  @Override
  public IBasePersistence<Product, Long> getPersistence() {
    return productPersistence;
  }

  @Override
  public ProductResponse create(
      HeaderContext context,
      ProductRequest request,
      TriConsumer<HeaderContext, Product, ProductRequest> validationCreateHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> mappingEntityHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> postHandler,
      BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler) {

    // Khởi tạo entity Product mới
    Product product = new Product();
    product.setName(request.getName());
    product.setSlug(SlugUtils.toSlug(request.getName()));
    product.setDescription(request.getDescription());
    product.setImageUrls(request.getImageUrls());
    product.setPrice(request.getPrice());
    product.setSalePrice(request.getSalePrice());
    product.setSeason(request.getSeason());
    product.setJerseyType(request.getJerseyType());
    product.setIsFeatured(request.getIsFeatured());
    product.setCode("PD" + productRepository.getNextSequence());

    // Lấy các thực thể liên quan
    Category category = categoryService.getEntityById(context, request.getCategoryId());
    Team team = teamService.getEntityById(context, request.getTeamId());
    Material material = materialService.getEntityById(context, request.getMaterialId());

    // Gán các mối quan hệ
    product.setCategory(category);
    product.setTeam(team);
    product.setMaterial(material);

    // ✅ Lưu Product trước để có ID
    productRepository.save(product);

    // ✅ Sau khi đã lưu product, tạo danh sách ProductVariant
    List<ProductVariant> variants =
        request.getVariants().stream()
            .map(
                variantReq -> {
                  ProductVariant variant = new ProductVariant();
                  variant.setProduct(product); // Gán product đã có ID
                  variant.setSize(variantReq.getSize());
                  variant.setStockQuantity(variantReq.getStockQuantity());
                  variant.setPriceAdjustment(variantReq.getPriceAdjustment());
                  return variant;
                })
            .collect(Collectors.toList());

    // Lưu danh sách variant
    productVariantRepository.saveAll(variants);

    // Gọi post handler nếu cần
    if (postHandler != null) {
      postHandler.accept(context, product, request);
    }

    // ✅ Trả về ProductResponse
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getSlug(),
        product.getCategory(),
        product.getImageUrls(),
        product.getTeam(),
        product.getMaterial(),
        product.getSeason(),
        product.getJerseyType(),
        product.getIsFeatured(),
        product.getSlug(),
        product.getDescription(),
        product.getPrice(),
        product.getSalePrice(),
        product.getCreatedAt(),
        product.getUpdatedAt(),
        product.getIsDeleted(),
        productVariantRepository.findByProductId(product.getId()));
  }

  @Override
  public ProductResponse update(
      HeaderContext context,
      Long aLong,
      ProductRequest request,
      QuadConsumer<HeaderContext, Long, Product, ProductRequest> validationHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> mappingHandler,
      PentaConsumer<HeaderContext, Product, Product, Long, ProductRequest> postHandler,
      BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler) {
    return IBaseService.super.update(
        context,
        aLong,
        request,
        validationHandler,
        mappingHandler,
        postHandler,
        mappingResponseHandler);
  }

  @Override
  public Page<ProductResponse> getAll(
      HeaderContext context,
      String search,
      Integer page,
      Integer pageSize,
      String sort,
      String filter,
      BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler) {
    return IBaseService.super.getAll(
        context, search, page, pageSize, sort, filter, mappingResponseHandler());
  }

  private BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler() {
    return (context, product) ->
        ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .slug(product.getSlug())
            .category(product.getCategory())
            .team(product.getTeam())
            .material(product.getMaterial())
            .imageUrls(product.getImageUrls())
            .season(product.getSeason())
            .jerseyType(product.getJerseyType())
            .isFeatured(product.getIsFeatured())
            .description(product.getDescription())
            .price(product.getPrice())
            .salePrice(product.getSalePrice())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .isDeleted(product.getIsDeleted())
            .variants(productVariantRepository.findByProductId(product.getId()))
            .build();
  }
}
