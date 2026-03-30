package poly.edu.vantix_hrm.dto.employee;

import jakarta.validation.constraints.*;
import lombok.*;
import poly.edu.vantix_hrm.entity.Employee.Gender;
import poly.edu.vantix_hrm.entity.Employee.WorkStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    // User thông tin
    @NotBlank(message = "Username không được để trống!")
    private String username;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự!")
    private String password; // Cho phép null khi update

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Email không hợp lệ!")
    private String email;

    @NotNull(message = "Vui lòng chọn vai trò!")
    private Long roleId;

    // Employee thông tin
    @NotBlank(message = "Họ tên không được để trống!")
    private String fullName;

    @NotNull(message = "Vui lòng chọn giới tính!")
    private Gender gender;

    @NotNull(message = "Ngày sinh không được để trống!")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ!")
    private LocalDate birthDate;

    @NotBlank(message = "Số điện thoại không được để trống!")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống!")
    private String address;

    @NotNull(message = "Vui lòng chọn phòng ban!")
    private Long departmentId;

    @NotNull(message = "Vui lòng chọn chức vụ!")
    private Long positionId;

    @Builder.Default
    private WorkStatus workStatus = WorkStatus.WORKING;


}