package poly.edu.vantix.entity.enums;

/*
 * PayrollStatus - trạng thái của kỳ lương hoặc phiếu lương
 * DRAFT: Đang tính / chưa chốt
 * CALCULATED: Đã tính xong, chờ duyệt
 * APPROVED: Đã duyệt, chờ chi trả
 * PAID: Đã thanh toán
 * CANCELLED: Đã hủy
 */
public enum PayrollStatus {
    DRAFT,
    CALCULATED,
    APPROVED,
    PAID,
    CANCELLED
}
