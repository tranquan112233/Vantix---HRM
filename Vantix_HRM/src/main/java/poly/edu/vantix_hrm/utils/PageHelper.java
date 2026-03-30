package poly.edu.vantix_hrm.utils;

import org.springframework.data.domain.*;
import poly.edu.vantix_hrm.dto.page.PageRequestDTO;
import poly.edu.vantix_hrm.dto.page.PageResponseDTO;

/*
 * PageHelper
 * -------------------------------------------
 * Tiện ích xử lý phân trang
 *
 * Gồm 2 chức năng:
 *   - createPageable : PageRequestDTO → Pageable  (truyền vào Repository)
 *   - toResponse     : Page<T>        → PageResponseDTO<T>  (trả về FE)
 *
 * Cách dùng trong Service:
 *   Pageable pageable = PageHelper.createPageable(request);
 *   Page<User> page   = userRepository.findAll(spec, pageable);
 *   return PageHelper.toResponse(page);
 */
public class PageHelper {

    // Utility class — không cho khởi tạo
    private PageHelper() {}

    // Tạo Pageable từ PageRequestDTO
    public static Pageable createPageable(PageRequestDTO request) {
        Sort sort = "desc".equalsIgnoreCase(request.getSortDir())
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    // Chuyển Page<T> → PageResponseDTO<T>
    public static <T> PageResponseDTO<T> toResponse(Page<T> page) {
        return PageResponseDTO.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}