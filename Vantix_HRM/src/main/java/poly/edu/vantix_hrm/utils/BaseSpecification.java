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

    // Tìm kiếm keyword trên nhiều field (không phân biệt hoa thường)
    public static <T> Specification<T> search(String keyword, String... fields) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // không lọc
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(
                        cb.like(cb.lower(root.get(field)), "%" + keyword.toLowerCase() + "%")
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Lọc theo giá trị chính xác
    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction(); // không lọc
            }
            return cb.equal(root.get(field), value);
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