package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import com.eps.shared.utils.functions.PentaConsumer;
import com.eps.shared.utils.functions.QuadConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.CategoryRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.CategoryResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Category;
import org.longg.nh.kickstyleecommerce.domain.persistence.CategoryPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.utils.SlugUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class CategoryService
        implements IBaseService<Category, Long, CategoryResponse, CategoryRequest, CategoryResponse> {

    private final CategoryPersistence categoryPersistence;

    @Override
    public IBasePersistence<Category, Long> getPersistence() {
        return categoryPersistence;
    }

    @Override
    public CategoryResponse create(
            HeaderContext context,
            CategoryRequest request,
            TriConsumer<HeaderContext, Category, CategoryRequest> validationCreateHandler,
            TriConsumer<HeaderContext, Category, CategoryRequest> mappingEntityHandler,
            TriConsumer<HeaderContext, Category, CategoryRequest> postHandler,
            BiFunction<HeaderContext, Category, CategoryResponse> mappingResponseHandler) {

        return IBaseService.super.create(
                context,
                request,
                validationCreateHandler,
                wrapMappingHandlerWithSlug(mappingEntityHandler),
                postHandler,
                categoryResponseMapper());
    }

    private final ProductRepository productRepository;

    @Override
    public void delete(HeaderContext context, Long aLong) {
        if (productRepository.existsByCategoryId(aLong)) {
            throw new ResponseException(HttpStatus.BAD_REQUEST,
                    "Không thể xóa danh mục này vì đang có sản phẩm sử dụng");
        }
        IBaseService.super.delete(context, aLong);
    }

    private BiFunction<HeaderContext, Category, CategoryResponse> categoryResponseMapper() {
        return (context, entity) ->
                CategoryResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .slug(entity.getSlug())
                        .createdAt(entity.getCreatedAt())
                        .isDeleted(entity.getIsDeleted())
                        .build();
    }


    @Override
    public CategoryResponse update(HeaderContext context, Long aLong, CategoryRequest request, QuadConsumer<HeaderContext, Long, Category, CategoryRequest> validationHandler, TriConsumer<HeaderContext, Category, CategoryRequest> mappingHandler, PentaConsumer<HeaderContext, Category, Category, Long, CategoryRequest> postHandler, BiFunction<HeaderContext, Category, CategoryResponse> mappingResponseHandler) {
        return IBaseService.super.update(context, aLong, request, validationHandler, mappingHandler, postHandler, mappingResponseHandler);
    }

    @Override
    public String[] getSearchFieldNames() {
        return new String[]{"name"};
    }

    private TriConsumer<HeaderContext, Category, CategoryRequest> wrapMappingHandlerWithSlug(
            TriConsumer<HeaderContext, Category, CategoryRequest> originalHandler) {
        return (ctx, category, req) -> {
            originalHandler.accept(ctx, category, req);

            if (category.getName() != null
                    && (category.getSlug() == null || category.getSlug().isBlank())) {
                category.setSlug(SlugUtils.toSlug(category.getName()));
            }
        };
    }
}
