package poly.edu.vantix_hrm.dto.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequestDTO {

    @NotBlank(message = "Tên chức vụ không được để trống!")
    private String name;

    private String description;

    @NotNull(message = "Vui lòng chọn phòng ban!")
    private Long departmentId;
}