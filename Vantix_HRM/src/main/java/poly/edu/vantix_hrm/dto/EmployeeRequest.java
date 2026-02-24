package poly.edu.vantix_hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import poly.edu.vantix_hrm.entity.Employee;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    // ========================
    // Employee info
    // ========================

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private Employee.Gender gender;

    private LocalDate birthDate;

    private String phone;

    private String address;

    @NotNull(message = "Trạng thái làm việc không được để trống")
    private Employee.WorkStatus workStatus;

    @NotNull(message = "Vui lòng chọn phòng ban")
    private Integer departmentId;

    @NotNull(message = "Vui lòng chọn chức vụ")
    private Integer positionId;


    // ========================
    // Account info
    // ========================

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @NotNull(message = "Vui lòng chọn role")
    private Integer roleId;

}