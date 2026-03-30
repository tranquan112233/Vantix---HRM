import api from "@/services/axios"

class AuthService {

    // =========================
    // LOGIN
    // =========================
    async login(data) {
        const res = await api.post("/auth/login", data)
        return res.data
        // Lưu ý: token được lưu bởi auth.store, không lưu ở đây
    }

    // =========================
    // GET CURRENT USER
    // =========================
    async me() {
        const res = await api.get("/auth/me")
        return res.data
    }

    // =========================
    // FORGOT PASSWORD
    // =========================
    async forgotPassword(email) {
        return api.post("/auth/forgot-password", { email })
    }

    // =========================
    // VERIFY OTP
    // =========================
    async verifyOtp(data) {
        // Fix: bỏ wrap { data } → gửi thẳng object { email, otp }
        const res = await api.post("/auth/verify-otp", data)
        return res.data
    }

    // =========================
    // RESET PASSWORD
    // =========================
    async resetPassword(data) {
        return api.post("/auth/reset-password", data)
    }

    // =========================
    // LOGOUT
    // =========================
    logout() {
        localStorage.removeItem("token")
    }

    // =========================
    // GET TOKEN
    // =========================
    getToken() {
        return localStorage.getItem("token")
    }

    // =========================
    // CHECK LOGIN
    // =========================
    isAuthenticated() {
        return !!localStorage.getItem("token")
    }

}

export default new AuthService()