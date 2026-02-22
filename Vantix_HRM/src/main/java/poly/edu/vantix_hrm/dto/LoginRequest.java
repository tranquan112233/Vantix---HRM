package poly.edu.vantix_hrm.DTO;

    import lombok.Data;

    // class này dùng để nhận dữ liệu từ FE nhé anh em,
    @Data
    public class LoginRequest {
        private String username;
        private String password;
    }
