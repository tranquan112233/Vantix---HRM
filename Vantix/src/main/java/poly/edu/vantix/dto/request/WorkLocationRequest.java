package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkLocationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotNull(message = "Radius is required")
    @Min(value = 10, message = "Radius must be at least 10 meters")
    private Integer radiusMeters;
}
