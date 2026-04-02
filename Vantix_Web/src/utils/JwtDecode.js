import { jwtDecode } from "jwt-decode";

export const getUser = () => {
    // 1. Lấy token (hỗ trợ cả 2 trường hợp lưu key là "token" hoặc "accessToken")
    const token = localStorage.getItem("token") || localStorage.getItem("accessToken");

    if (!token) {
        console.warn("⚠️ Không tìm thấy Token trong LocalStorage!");
        return null;
    }

    try {
        const decoded = jwtDecode(token);

        // 2. Kiểm tra token hết hạn (Code hay từ hàm 2)
        // Lưu ý: exp tính bằng giây, Date.now() tính bằng mili-giây
        if (decoded.exp && (decoded.exp * 1000 < Date.now())) {
            console.warn("⚠️ Token đã hết hạn! Tự động xóa token...");
            localStorage.removeItem("token");
            localStorage.removeItem("accessToken");
            return null;
        }

        // 3. Log ra để anh nhìn tận mắt các trường trong Token
        console.log("💎 Decoded Token:", decoded);

        // 4. Trả về object đã được chuẩn hóa (Code hay từ hàm 1 + gộp nguyên bản)
        return {
            ...decoded, // Bê nguyên toàn bộ các trường gốc trong token trả về
            // Tạo thêm trường 'id' dự phòng nếu Spring Boot trả về tên khác
            id: decoded.id || decoded.sub || decoded.userId || decoded.employeeId,
            fullName: decoded.fullName,
            role: decoded.role
        };
    } catch (error) {
        console.error("❌ Lỗi giải mã Token:", error);
        // Token sai định dạng thì xóa luôn cho an toàn
        localStorage.removeItem("token");
        localStorage.removeItem("accessToken");
        return null;
    }
};

export function getRole() {
    return getUser()?.role || null
}