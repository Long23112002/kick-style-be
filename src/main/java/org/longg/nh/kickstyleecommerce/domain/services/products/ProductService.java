package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import com.eps.shared.utils.functions.PentaConsumer;
import com.eps.shared.utils.functions.QuadConsumer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductVariantRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ProductResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.persistence.ProductPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.longg.nh.kickstyleecommerce.domain.utils.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

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

  /** Validate tên sản phẩm không trùng lặp */
  private void validateProductName(String name, Long excludeProductId) {
    String slug = SlugUtils.toSlug(name);
    boolean exists;

    if (excludeProductId == null) {
      exists = productRepository.existsBySlug(slug);
    } else {
      exists = productRepository.existsBySlugAndIdNot(slug, excludeProductId);
    }

    if (exists) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Tên sản phẩm '" + name + "' đã tồn tại");
    }
  }

  @Override
  public void postCreateHandler(HeaderContext context, Product entity, ProductRequest request) {

    IBaseService.super.postCreateHandler(context, entity, request);
  }

  /** Validate các size trong variants không trùng lặp trong cùng 1 product */
  private void validateVariantSizes(List<ProductVariantRequest> variants) {
    Set<String> normalizedSizes = new HashSet<>();

    for (ProductVariantRequest variant : variants) {
      // Normalize size: lowercase và trim space
      String normalizedSize = variant.getSize().toLowerCase().trim();

      // Check trùng lặp trong cùng request (cùng 1 product)
      if (!normalizedSizes.add(normalizedSize)) {
        throw new ResponseException(
            HttpStatus.BAD_REQUEST,
            "Size '"
                + variant.getSize()
                + "' bị trùng lặp trong danh sách variants của sản phẩm này");
      }
    }
  }

  @Override
  public ProductResponse create(
      HeaderContext context,
      ProductRequest request,
      TriConsumer<HeaderContext, Product, ProductRequest> validationCreateHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> mappingEntityHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> postHandler,
      BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler) {

    // Validate tên sản phẩm không trùng
    validateProductName(request.getName(), null);

    // Validate sizes trong variants không trùng
    validateVariantSizes(request.getVariants());

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

                  // Xử lý priceAdjustment: nếu = 0 hoặc null thì set = 0 (nghĩa là lấy giá từ
                  // product)
                  BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
                  if (priceAdjustment == null) {
                    priceAdjustment = BigDecimal.ZERO;
                  }
                  variant.setPriceAdjustment(priceAdjustment);

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
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .slug(product.getSlug())
        .category(product.getCategory())
        .imageUrls(product.getImageUrls())
        .team(product.getTeam())
        .material(product.getMaterial())
        .season(product.getSeason())
        .jerseyType(product.getJerseyType())
        .isFeatured(product.getIsFeatured())
        .code(product.getCode())
        .description(product.getDescription())
        .price(product.getPrice())
        .salePrice(product.getSalePrice())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .isDeleted(product.getIsDeleted())
        .variants(productVariantRepository.findByProductId(product.getId()))
        .build();
  }

  @Override
  public ProductResponse update(
      HeaderContext context,
      Long productId,
      ProductRequest request,
      QuadConsumer<HeaderContext, Long, Product, ProductRequest> validationHandler,
      TriConsumer<HeaderContext, Product, ProductRequest> mappingHandler,
      PentaConsumer<HeaderContext, Product, Product, Long, ProductRequest> postHandler,
      BiFunction<HeaderContext, Product, ProductResponse> mappingResponseHandler) {

    // Lấy product hiện tại
    Product existingProduct =
        productRepository
            .findById(productId)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Product không tồn tại với ID: " + productId));

    // Validate tên sản phẩm không trùng (trừ chính nó)
    validateProductName(request.getName(), productId);

    // Validate sizes trong variants không trùng
    validateVariantSizes(request.getVariants());

    // Lưu product cũ để dùng trong post handler
    Product oldProduct = new Product();
    oldProduct.setId(existingProduct.getId());
    oldProduct.setName(existingProduct.getName());
    oldProduct.setSlug(existingProduct.getSlug());
    oldProduct.setCode(existingProduct.getCode());

    // Cập nhật thông tin product (KHÔNG update code vì nó là unique identifier)
    existingProduct.setName(request.getName());
    existingProduct.setSlug(SlugUtils.toSlug(request.getName()));
    existingProduct.setDescription(request.getDescription());
    existingProduct.setImageUrls(request.getImageUrls());
    existingProduct.setPrice(request.getPrice());
    existingProduct.setSalePrice(request.getSalePrice());
    existingProduct.setSeason(request.getSeason());
    existingProduct.setJerseyType(request.getJerseyType());
    existingProduct.setIsFeatured(request.getIsFeatured());

    // Lấy các thực thể liên quan
    Category category = categoryService.getEntityById(context, request.getCategoryId());
    Team team = teamService.getEntityById(context, request.getTeamId());
    Material material = materialService.getEntityById(context, request.getMaterialId());

    // Gán các mối quan hệ
    existingProduct.setCategory(category);
    existingProduct.setTeam(team);
    existingProduct.setMaterial(material);

    // Gọi validation handler nếu có
    if (validationHandler != null) {
      validationHandler.accept(context, productId, existingProduct, request);
    }

    // Gọi mapping handler nếu có
    if (mappingHandler != null) {
      mappingHandler.accept(context, existingProduct, request);
    }

    // Lưu product
    productRepository.save(existingProduct);

    // Cập nhật variants một cách thông minh
    updateProductVariants(existingProduct, request.getVariants());

    // Gọi post handler nếu có
    if (postHandler != null) {
      postHandler.accept(context, existingProduct, oldProduct, productId, request);
    }

    // Trả về ProductResponse
    return mappingResponseHandler().apply(context, existingProduct);
  }

  /**
   * Cập nhật variants một cách thông minh: - Cập nhật variants có sẵn - Thêm variants mới - Xóa
   * variants không còn trong request
   */
  private void updateProductVariants(Product product, List<ProductVariantRequest> variantRequests) {
    // Lấy danh sách variants hiện tại
    List<ProductVariant> existingVariants =
        productVariantRepository.findByProductId(product.getId());

    // Tạo map để dễ lookup variants hiện tại theo size
    Map<String, ProductVariant> existingVariantMap =
        existingVariants.stream()
            .collect(
                Collectors.toMap(
                    variant -> variant.getSize().toLowerCase().trim(), variant -> variant));

    // Danh sách variants sẽ được lưu
    List<ProductVariant> variantsToSave = new ArrayList<>();

    // Danh sách size trong request (normalized)
    Set<String> requestSizes = new HashSet<>();

    for (ProductVariantRequest variantReq : variantRequests) {
      String normalizedSize = variantReq.getSize().toLowerCase().trim();
      requestSizes.add(normalizedSize);

      ProductVariant variant = existingVariantMap.get(normalizedSize);

      if (variant != null) {
        // Cập nhật variant có sẵn
        variant.setSize(variantReq.getSize()); // Giữ nguyên case gốc
        variant.setStockQuantity(variantReq.getStockQuantity());

        BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
        if (priceAdjustment == null) {
          priceAdjustment = BigDecimal.ZERO;
        }
        variant.setPriceAdjustment(priceAdjustment);
      } else {
        // Tạo variant mới
        variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(variantReq.getSize());
        variant.setStockQuantity(variantReq.getStockQuantity());

        BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
        if (priceAdjustment == null) {
          priceAdjustment = BigDecimal.ZERO;
        }
        variant.setPriceAdjustment(priceAdjustment);
      }

      variantsToSave.add(variant);
    }

    // Xóa các variants không còn trong request
    List<ProductVariant> variantsToDelete =
        existingVariants.stream()
            .filter(variant -> !requestSizes.contains(variant.getSize().toLowerCase().trim()))
            .collect(Collectors.toList());

    if (!variantsToDelete.isEmpty()) {
      productVariantRepository.deleteAll(variantsToDelete);
    }

    // Lưu tất cả variants (cập nhật + mới)
    productVariantRepository.saveAll(variantsToSave);
  }

  public ProductResponse findById(Long id){
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Product không tồn tại với ID: " + id));

    return mappingResponseHandler().apply(null, product);
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



  @Override
  public List<Predicate> buildFilterQuery(
          Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb, Map<String, Object> filter) {

    List<Predicate> predicates = new ArrayList<>(IBaseService.super.buildFilterQuery(root, query, cb, filter));

    boolean hasMin = filter.containsKey("minPrice");
    boolean hasMax = filter.containsKey("maxPrice");

    try {
      if (hasMin && hasMax) {
        Double minPrice = Double.parseDouble(filter.get("minPrice").toString());
        Double maxPrice = Double.parseDouble(filter.get("maxPrice").toString());
        predicates.add(cb.between(root.get("price"), minPrice, maxPrice));
      } else if (hasMin) {
        Double minPrice = Double.parseDouble(filter.get("minPrice").toString());
        predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
      } else if (hasMax) {
        Double maxPrice = Double.parseDouble(filter.get("maxPrice").toString());
        predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
      }
    } catch (NumberFormatException e) {
      if (hasMin && hasMax) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Giá min hoặc max không hợp lệ");
      } else if (hasMin) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Giá min không hợp lệ");
      } else if (hasMax) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Giá max không hợp lệ");
      }
    }

    return predicates;
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
            .code(product.getCode())
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
