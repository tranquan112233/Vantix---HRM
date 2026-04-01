package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_bank_accounts") // Quản lý thông tin tài khoản ngân hàng của nhân viên
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId; // ID bản ghi

    // Sử dụng OneToOne vì thông thường mỗi nhân viên chỉ dùng 1 tài khoản chính để nhận lương
    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee; // Nối với Entity Employee của bạn

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber; // Số tài khoản ngân hàng

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName; // Tên ngân hàng (VD: Techcombank, Vietcombank, MB Bank)

    @Column(name = "account_owner_name", nullable = false, length = 100)
    private String accountOwnerName; // Tên chủ tài khoản (Nên lưu dạng IN HOA KHÔNG DẤU)

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // Thời gian cập nhật cuối
}
