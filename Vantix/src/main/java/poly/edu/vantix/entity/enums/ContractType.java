package poly.edu.vantix.entity.enums;

/*
 * ContractType
 * - Phân loại hợp đồng lao động theo Bộ luật Lao động 2019
 * - INDEFINITE: HĐ không xác định thời hạn
 * - FIXED_TERM: HĐ xác định thời hạn (tối đa 36 tháng)
 * - SEASONAL: HĐ thời vụ / dưới 12 tháng (áp dụng trước 2021, giữ lại cho dữ liệu cũ)
 * - PROBATION: HĐ thử việc (Điều 25 BLLĐ)
 * - APPRENTICESHIP: HĐ học việc / đào tạo nghề
 * - PART_TIME: HĐ làm việc bán thời gian
 * - SERVICE: HĐ khoán việc / dịch vụ
 */
public enum ContractType {
    INDEFINITE,
    FIXED_TERM,
    SEASONAL,
    PROBATION,
    APPRENTICESHIP,
    PART_TIME,
    SERVICE
}
