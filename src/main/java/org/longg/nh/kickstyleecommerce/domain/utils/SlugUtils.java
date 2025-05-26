package org.longg.nh.kickstyleecommerce.domain.utils;

import java.text.Normalizer;

public class SlugUtils {
  public static String toSlug(String input) {
    if (input == null) return null;

    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

    String slug =
        withoutAccents
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-");

    return slug.replaceAll("^-|-$", "");
  }
}
