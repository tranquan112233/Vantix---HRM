import { jwtDecode } from "jwt-decode";

export const getUser = () => {
    // 1. Kiểm tra chính xác tên Key anh lưu trong LocalStorage lúc Login là gì?
    const token = localStorage.getItem("token") || localStorage.getItem("accessToken");

    if (!token) {
        console.warn("⚠️ Không tìm thấy Token trong LocalStorage!");
        return null;
    }

    try {
        const decoded = jwtDecode(token);
        // Log ra để anh nhìn tận mắt các trường trong Token (id, sub, userId, hay employeeId?)
        console.log("💎 Decoded Token:", decoded);

        return {
            // Spring Boot thường để ID vào 'id' hoặc 'sub'. Anh check console rồi sửa chỗ này cho đúng
            id: decoded.id || decoded.sub || decoded.userId,
            fullName: decoded.fullName,
            role: decoded.role
        };
    } catch (error) {
        console.error("❌ Lỗi giải mã Token:", error);
        return null;
    }
};

export function getUser() {
    const token = localStorage.getItem("token")
    if (!token) return null

    try {
        const decoded = jwtDecode(token)

        // kiểm tra hết hạn
        if (decoded.exp * 1000 < Date.now()) {
            localStorage.removeItem("token")
            return null
        }

        return decoded
    } catch (error) {
        localStorage.removeItem("token")
        return null
    }
}

export function getRole() {
    return getUser()?.role || null
}