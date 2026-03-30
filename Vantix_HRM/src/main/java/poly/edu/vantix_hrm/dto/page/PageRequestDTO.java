package poly.edu.vantix_hrm.dto.page;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequestDTO {

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    // "asc" hoặc "desc"
    @Builder.Default
    private String sortDir = "desc";
}