import { jwtDecode } from "jwt-decode";

export function getUserIdFromToken(){

    const token = localStorage.getItem("token");

    if(!token) return null;

    const decoded = jwtDecode(token);

    return decoded.sub;
}

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