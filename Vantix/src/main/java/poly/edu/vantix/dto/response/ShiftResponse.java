package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Shift;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {

    private Long id;
    private String code;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;

    public static ShiftResponse fromEntity(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .code(shift.getCode())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .description(shift.getDescription())
                .build();
    }
}
