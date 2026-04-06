package poly.edu.vantix_hrm.utils;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * BaseSpecification
 * -------------------------------------------
 * Tiện ích tạo điều kiện query động cho JPA
 *
 * Gồm 4 loại điều kiện:
 *   - search    : tìm kiếm keyword trên nhiều field (LIKE)
 *   - equal     : lọc theo giá trị chính xác
 *   - between   : lọc theo khoảng thời gian
 *   - isNotDeleted : chỉ lấy bản ghi chưa bị xóa mềm
 *
 * Cách dùng trong Service:
 *   Specification<User> spec = Specification
 *       .where(BaseSpecification.search(keyword, "fullName", "email"))
 *       .and(BaseSpecification.equal("status", status))
 *       .and(BaseSpecification.isNotDeleted());
 */
public class BaseSpecification {

    /**
     * Hỗ trợ path lồng nhau (dot-notation): "user.email", "department.id"
     * → root.get("user").get("email"), root.get("department").get("id")
     */
    @SuppressWarnings("unchecked")
    private static <T> jakarta.persistence.criteria.Path<Object> resolvePath(
            jakarta.persistence.criteria.Root<T> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        jakarta.persistence.criteria.Path<?> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return (jakarta.persistence.criteria.Path<Object>) path;
    }

    // Tìm kiếm keyword trên nhiều field (không phân biệt hoa thường, hỗ trợ nested path)
    public static <T> Specification<T> search(String keyword, String... fields) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // không lọc
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(
                        cb.like(cb.lower(resolvePath(root, field).as(String.class)),
                                "%" + keyword.toLowerCase() + "%")
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Lọc theo giá trị chính xác (hỗ trợ nested path: "department.id")
    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction(); // không lọc
            }
            return cb.equal(resolvePath(root, field), value);
        };
    }

    // Lọc theo khoảng thời gian
    public static <T> Specification<T> between(String field, LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null || end == null) {
                return cb.conjunction(); // không lọc
            }
            return cb.between(root.get(field), start, end);
        };
    }

    // Chỉ lấy bản ghi chưa bị xóa mềm (deleted = false)
    public static <T> Specification<T> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}