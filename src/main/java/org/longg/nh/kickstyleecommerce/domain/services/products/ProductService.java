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
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ProductVariantRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ProductResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Status;
import org.longg.nh.kickstyleecommerce.domain.persistence.ProductPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartItemRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.SizesRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ColorsRepository;
import org.longg.nh.kickstyleecommerce.domain.services.cart.CartItemService;
import org.longg.nh.kickstyleecommerce.domain.utils.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService
    implements IBaseService<Product, Long, ProductResponse, ProductRequest, ProductResponse> {

  private final ProductPersistence productPersistence;
  private final ProductRepository productRepository;
  private final CategoryService categoryService;
  private final TeamService teamService;
  private final MaterialService materialService;
  private final ProductVariantRepository productVariantRepository;
  private final SizesRepository sizesRepository;
  private final ColorsRepository colorsRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  public IBasePersistence<Product, Long> getPersistence() {
    return productPersistence;
  }

  @Override
  public void validateDelete(HeaderContext context, Long aLong, Product entity) {
    cartItemRepository.deleteAllByProductId(aLong);
    IBaseService.super.validateDelete(context, aLong, entity);
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

  /** Validate các size và color trong variants không trùng lặp trong cùng 1 product */
  private void validateVariantSizeAndColor(List<ProductVariantRequest> variants) {
    Set<String> sizeColorCombinations = new HashSet<>();

    for (ProductVariantRequest variant : variants) {
      // Kiểm tra sizeId và colorId có tồn tại không
      if (!sizesRepository.existsById(variant.getSizeId())) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Size ID " + variant.getSizeId() + " không tồn tại");
      }
      
      if (!colorsRepository.existsById(variant.getColorId())) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Color ID " + variant.getColorId() + " không tồn tại");
      }

      // Tạo unique key từ sizeId và colorId
      String sizeColorKey = variant.getSizeId() + "_" + variant.getColorId();

      // Check trùng lặp combination size + color trong cùng request
      if (!sizeColorCombinations.add(sizeColorKey)) {
        throw new ResponseException(
            HttpStatus.BAD_REQUEST,
            "Combination size ID " + variant.getSizeId() + " và color ID " + variant.getColorId() 
            + " bị trùng lặp trong danh sách variants của sản phẩm này");
      }
    }
  }

  /**
   * Tự động cập nhật status của variant dựa trên stockQuantity
   */
  private Status determineVariantStatus(Integer stockQuantity) {
    return stockQuantity != null && stockQuantity <= 0 ? Status.OUT_OF_STOCK : Status.ACTIVE;
  }

  /**
   * Tự động cập nhật status của product dựa trên status của các variants
   */
  private void updateProductStatus(Product product) {
    List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

    if (variants.isEmpty()) {
      product.setStatus(Status.INACTIVE);
      return;
    }

    boolean allOutOfStock = variants.stream()
            .allMatch(variant -> variant.getStatus() == Status.OUT_OF_STOCK);

    if (allOutOfStock) {
      product.setStatus(Status.OUT_OF_STOCK);
      return;
    }

    Status requestedStatus = product.getStatus();

    if (requestedStatus == Status.ACTIVE || requestedStatus == Status.INACTIVE) {
      product.setStatus(requestedStatus);
    } else {
      boolean hasActiveVariant = variants.stream()
              .anyMatch(variant -> variant.getStatus() == Status.ACTIVE);

      product.setStatus(hasActiveVariant ? Status.ACTIVE : Status.INACTIVE);
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
    validateVariantSizeAndColor(request.getVariants());

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
    
    // Set status từ request hoặc mặc định ACTIVE (sẽ được cập nhật sau dựa trên variants)
    product.setStatus(request.getStatus() != null ? request.getStatus() : Status.ACTIVE);

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
                  
                  // Lấy Size và Color entity
                  Sizes size = sizesRepository.findById(variantReq.getSizeId())
                      .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
                          "Size ID " + variantReq.getSizeId() + " không tồn tại"));
                  Colors color = colorsRepository.findById(variantReq.getColorId())
                      .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
                          "Color ID " + variantReq.getColorId() + " không tồn tại"));
                  
                  variant.setSize(size);
                  variant.setColor(color);
                  variant.setStockQuantity(variantReq.getStockQuantity());

                  // Xử lý priceAdjustment: nếu = 0 hoặc null thì set = 0 (nghĩa là lấy giá từ
                  // product)
                  BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
                  if (priceAdjustment == null) {
                    priceAdjustment = BigDecimal.ZERO;
                  }
                  variant.setPriceAdjustment(priceAdjustment);

                  // Tự động set status dựa trên stockQuantity
                  variant.setStatus(determineVariantStatus(variantReq.getStockQuantity()));

                  return variant;
                })
            .collect(Collectors.toList());

    // Lưu danh sách variant
    productVariantRepository.saveAll(variants);

    // Cập nhật status của product dựa trên variants (ưu tiên tự động tính toán)
    updateProductStatus(product);
    productRepository.save(product);

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
        .status(product.getStatus())
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
    validateVariantSizeAndColor(request.getVariants());

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

    // Cập nhật status của product dựa trên variants sau khi cập nhật
    updateProductStatus(existingProduct);
    productRepository.save(existingProduct);

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

    // Tạo map để dễ lookup variants hiện tại theo sizeId_colorId
    Map<String, ProductVariant> existingVariantMap =
        existingVariants.stream()
            .collect(
                Collectors.toMap(
                    variant -> variant.getSize().getId() + "_" + variant.getColor().getId(), 
                    variant -> variant));

    // Danh sách variants sẽ được lưu
    List<ProductVariant> variantsToSave = new ArrayList<>();

    // Danh sách size_color combination trong request
    Set<String> requestSizeColorKeys = new HashSet<>();

    for (ProductVariantRequest variantReq : variantRequests) {
      String sizeColorKey = variantReq.getSizeId() + "_" + variantReq.getColorId();
      requestSizeColorKeys.add(sizeColorKey);

      ProductVariant variant = existingVariantMap.get(sizeColorKey);

      if (variant != null) {
        // Cập nhật variant có sẵn
        variant.setStockQuantity(variantReq.getStockQuantity());

        BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
        if (priceAdjustment == null) {
          priceAdjustment = BigDecimal.ZERO;
        }
        variant.setPriceAdjustment(priceAdjustment);

        // Tự động cập nhật status dựa trên stockQuantity
        variant.setStatus(determineVariantStatus(variantReq.getStockQuantity()));
      } else {
        // Tạo variant mới
        variant = new ProductVariant();
        variant.setProduct(product);
        
        // Lấy Size và Color entity
        Sizes size = sizesRepository.findById(variantReq.getSizeId())
            .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
                "Size ID " + variantReq.getSizeId() + " không tồn tại"));
        Colors color = colorsRepository.findById(variantReq.getColorId())
            .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
                "Color ID " + variantReq.getColorId() + " không tồn tại"));
        
        variant.setSize(size);
        variant.setColor(color);
        variant.setStockQuantity(variantReq.getStockQuantity());

        BigDecimal priceAdjustment = variantReq.getPriceAdjustment();
        if (priceAdjustment == null) {
          priceAdjustment = BigDecimal.ZERO;
        }
        variant.setPriceAdjustment(priceAdjustment);

        // Tự động set status dựa trên stockQuantity cho variant mới
        variant.setStatus(determineVariantStatus(variantReq.getStockQuantity()));
      }

      variantsToSave.add(variant);
    }

    // Xóa các variants không còn trong request
    List<ProductVariant> variantsToDelete =
        existingVariants.stream()
            .filter(variant -> !requestSizeColorKeys.contains(
                variant.getSize().getId() + "_" + variant.getColor().getId()))
            .collect(Collectors.toList());

    if (!variantsToDelete.isEmpty()) {
      log.info("Deleting {} variants that are no longer in the request", variantsToDelete.size());
      
      // Xóa cart items chứa các variant sẽ bị xóa
      for (ProductVariant variant : variantsToDelete) {
        try {
          log.info("Removing variant ID {} from all shopping carts", variant.getId());
          cartItemRepository.deleteAllByVariantId(variant.getId());
          log.info("Successfully removed variant ID {} from all shopping carts", variant.getId());
        } catch (Exception e) {
          log.error("Error removing variant ID {} from shopping carts: {}", variant.getId(), e.getMessage(), e);
          // Continue with deletion even if cart item deletion fails
        }
      }
      
      productVariantRepository.deleteAll(variantsToDelete);
      log.info("Successfully deleted {} variants", variantsToDelete.size());
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

  /**
   * Cập nhật status của một product và tất cả variants của nó
   * Method này có thể được gọi từ bên ngoài khi cần đồng bộ status
   */
  public void updateProductAndVariantsStatus(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
            "Product không tồn tại với ID: " + productId));

    // Cập nhật status của tất cả variants dựa trên stockQuantity
    List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
    variants.forEach(variant -> {
      variant.setStatus(determineVariantStatus(variant.getStockQuantity()));
    });
    
    if (!variants.isEmpty()) {
      productVariantRepository.saveAll(variants);
    }

    // Cập nhật status của product dựa trên variants
    updateProductStatus(product);
    productRepository.save(product);
  }

  /**
   * Cập nhật stock quantity và tự động cập nhật status
   */
  public void updateVariantStock(Long variantId, Integer newStockQuantity) {
    ProductVariant variant = productVariantRepository.findById(variantId)
        .orElseThrow(() -> new ResponseException(HttpStatus.BAD_REQUEST, 
            "Product variant không tồn tại với ID: " + variantId));

    variant.setStockQuantity(newStockQuantity);
    variant.setStatus(determineVariantStatus(newStockQuantity));
    productVariantRepository.save(variant);

    // Cập nhật status của product
    updateProductStatus(variant.getProduct());
    productRepository.save(variant.getProduct());
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

    // Filter theo giá
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

    // Filter theo categoryIds (mảng category IDs)
    if (filter.containsKey("categoryIds")) {
      try {
        Object categoryIdsObj = filter.get("categoryIds");
        if (categoryIdsObj instanceof List<?>) {
          @SuppressWarnings("unchecked")
          List<Long> categoryIds = (List<Long>) categoryIdsObj;
          if (!categoryIds.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categoryIds));
          }
        } else if (categoryIdsObj instanceof String) {
          // Trường hợp truyền string "1,2,3"
          String[] categoryIdStrings = categoryIdsObj.toString().split(",");
          List<Long> categoryIds = new ArrayList<>();
          for (String idStr : categoryIdStrings) {
            categoryIds.add(Long.parseLong(idStr.trim()));
          }
          if (!categoryIds.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categoryIds));
          }
        }
      } catch (Exception e) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Category IDs không hợp lệ");
      }
    }

    // Filter theo colorIds (qua ProductVariant)
    if (filter.containsKey("colorIds")) {
      try {
        Object colorIdsObj = filter.get("colorIds");
        List<Long> colorIds = parseArrayParameter(colorIdsObj);
        if (!colorIds.isEmpty()) {
          // Sử dụng EXISTS subquery để tránh duplicate records
          var subquery = query.subquery(ProductVariant.class);
          var subRoot = subquery.from(ProductVariant.class);
          subquery.select(subRoot)
                  .where(cb.and(
                      cb.equal(subRoot.get("product"), root),
                      subRoot.get("color").get("id").in(colorIds)
                  ));
          predicates.add(cb.exists(subquery));
        }
      } catch (Exception e) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Color IDs không hợp lệ");
      }
    }

    // Filter theo sizeIds (qua ProductVariant)
    if (filter.containsKey("sizeIds")) {
      try {
        Object sizeIdsObj = filter.get("sizeIds");
        List<Long> sizeIds = parseArrayParameter(sizeIdsObj);
        if (!sizeIds.isEmpty()) {
          var subquery = query.subquery(ProductVariant.class);
          var subRoot = subquery.from(ProductVariant.class);
          subquery.select(subRoot)
                  .where(cb.and(
                      cb.equal(subRoot.get("product"), root),
                      subRoot.get("size").get("id").in(sizeIds)
                  ));
          predicates.add(cb.exists(subquery));
        }
      } catch (Exception e) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Size IDs không hợp lệ");
      }
    }

    return predicates;
  }

  /**
   * Parse array parameter từ Object (có thể là List hoặc String)
   */
  private List<Long> parseArrayParameter(Object paramObj) {
    List<Long> result = new ArrayList<>();
    
    if (paramObj instanceof List<?>) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) paramObj;
      for (Object item : list) {
        if (item instanceof Number) {
          result.add(((Number) item).longValue());
        } else {
          result.add(Long.parseLong(item.toString()));
        }
      }
    } else if (paramObj instanceof String) {
      // Trường hợp truyền string "1,2,3"
      String[] idStrings = paramObj.toString().split(",");
      for (String idStr : idStrings) {
        result.add(Long.parseLong(idStr.trim()));
      }
    }
    
    return result;
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
            .status(product.getStatus())
            .salePrice(product.getSalePrice())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .isDeleted(product.getIsDeleted())
            .variants(productVariantRepository.findByProductId(product.getId()))
            .build();
  }

  /**
   * Tìm sản phẩm bằng ID kể cả khi đã soft-delete, dành riêng cho Orders
   * @param productId ID của sản phẩm
   * @return Product entity bao gồm cả sản phẩm đã soft-delete
   */
  public Product findProductForOrderById(Long productId) {
    return productRepository.findProductByIdIncludingDeleted(productId)
        .orElseThrow(() -> new ResponseException(
            HttpStatus.NOT_FOUND, 
            "Product không tồn tại với ID: " + productId + " (kể cả đã xóa)"));
  }

  /**
   * Tìm sản phẩm bằng ID kể cả khi đã soft-delete, dành riêng cho Orders
   * Trả về Optional nếu không muốn throw exception
   * @param productId ID của sản phẩm
   * @return Optional<Product> entity bao gồm cả sản phẩm đã soft-delete
   */
  public Optional<Product> findProductForOrderByIdOptional(Long productId) {
    return productRepository.findProductByIdIncludingDeleted(productId);
  }

  /**
   * Lấy danh sách variants của một product, bao gồm cả các variants của sản phẩm đã soft-delete
   * Dùng riêng cho Orders để đảm bảo orders vẫn hiển thị được đầy đủ thông tin sản phẩm
   * 
   * @param productId ID của sản phẩm
   * @return Danh sách variants của sản phẩm
   */
  public List<ProductVariant> findProductVariantsForOrder(Long productId) {
    try {
      // Sử dụng findByProductIdIncludingDeleted để lấy cả variants đã bị soft-delete
      return productVariantRepository.findByProductIdIncludingDeleted(productId);
    } catch (Exception e) {
      log.error("Error retrieving product variants for order: {}", e.getMessage());
    }
    return new ArrayList<>();
  }
  
  /**
   * Lấy ProductResponse từ product ID kể cả khi đã soft-delete, dành riêng cho Orders
   * Đảm bảo đầy đủ thông tin bao gồm cả variants
   * 
   * @param productId ID của sản phẩm
   * @return ProductResponse bao gồm cả sản phẩm đã soft-delete và variants
   */
  public ProductResponse findProductResponseForOrderById(Long productId) {
    Product product = findProductForOrderById(productId);
    
    // Chuẩn bị response với toàn bộ thông tin
    ProductResponse response = ProductResponse.builder()
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
        .status(product.getStatus())
        .salePrice(product.getSalePrice())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .isDeleted(product.getIsDeleted())
        .variants(findProductVariantsForOrder(product.getId()))
        .build();
    
    return response;
  }
}
