package poly.edu.vantix.entity.enums;

/*
 * ContractStatus
 * - DRAFT: Đang soạn thảo, chưa ký
 * - ACTIVE: Đang hiệu lực
 * - EXPIRED: Đã hết hạn theo ngày kết thúc
 * - TERMINATED: Bị chấm dứt trước hạn
 * - LIQUIDATED: Đã thanh lý (kết thúc toàn bộ nghĩa vụ)
 */
public enum ContractStatus {
    DRAFT,
    ACTIVE,
    EXPIRED,
    TERMINATED,
    LIQUIDATED
}
