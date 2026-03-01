package poly.edu.vantix_hrm.dto.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionResponse {
    private Integer positionId;
    private String positionName;
}
