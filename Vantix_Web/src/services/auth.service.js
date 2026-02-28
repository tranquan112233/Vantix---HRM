import api from "@/services/axios";

class AuthService {

    login(email, password) {

        return api.post("/auth/login", {
            email: email,
            password: password
        });
    }

    logout() {
        localStorage.removeItem("token");
    }

    getToken() {
        return localStorage.getItem("token");
    }

    saveToken(token) {
        localStorage.setItem("token", token);
    }

    forgotPassword(email) {
        return api.post(`/auth/forgot-password`,
            { email }
        )
    }

    verifyOtp(email, otp) {
        return api.post(`/auth/verify-otp`,
            {email, otp})
    }

    resetPassword(resetToken, newPassword, confirmPassword) {
        return api.post(`auth/reset-password`,
            {resetToken, newPassword, confirmPassword}
        )

    }

    isLoggedIn() {
        return !!this.getToken();
    }
}

export default new AuthService();