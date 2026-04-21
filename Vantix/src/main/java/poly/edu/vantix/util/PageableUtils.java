package poly.edu.vantix.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PageableUtils() {
    }

    public static boolean isPaged(Integer page, Integer size) {
        return page != null || size != null;
    }

    public static Pageable from(Integer page, Integer size) {
        int safePage = page == null ? DEFAULT_PAGE : Math.max(page, 0);
        int requestedSize = size == null ? DEFAULT_SIZE : size;
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
