package org.longg.nh.kickstyleecommerce.domain.utils;

import com.eps.shared.models.HeaderContext;
import org.apache.logging.log4j.util.TriConsumer;

public class MappingUtils {

  public static <TEntity, TRequest>
      TriConsumer<HeaderContext, TEntity, TRequest> wrapWithSlugHandler(
          TriConsumer<HeaderContext, TEntity, TRequest> originalHandler,
          java.util.function.Function<TEntity, String> getName,
          java.util.function.Supplier<String> getSlug,
          java.util.function.Consumer<String> setSlug) {

    return (ctx, entity, request) -> {
      originalHandler.accept(ctx, entity, request);

      String name = getName.apply(entity);
      String slug = getSlug.get();

      if (name != null && (slug == null || slug.isBlank())) {
        setSlug.accept(SlugUtils.toSlug(name));
      }
    };
  }
}
