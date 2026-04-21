package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.WorkLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLocationResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;

    public static WorkLocationResponse fromEntity(WorkLocation location) {
        return WorkLocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .radiusMeters(location.getRadiusMeters())
                .build();
    }
}
