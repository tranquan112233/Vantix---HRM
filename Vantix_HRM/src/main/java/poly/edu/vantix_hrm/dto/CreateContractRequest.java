package poly.edu.vantix_hrm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateContractRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private Integer employeeId;

    @NotBlank(message = "Vị trí công việc không được để trống")
    private String position;

    @NotNull(message = "Loại hợp đồng không được để trống")
    private Contract.Type type;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Lương cơ bản không được để trống")
    @Min(value = 0, message = "Lương cơ bản phải lớn hơn hoặc bằng 0")
    private BigDecimal baseSalary;

    @NotNull(message = "Trạng thái hợp đồng không được để trống")
    private Contract.ContractStatus status;
}