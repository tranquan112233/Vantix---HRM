package poly.edu.vantix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
 * Test smoke nhẹ, không phụ thuộc database local.
 */
class VantixApplicationTests {

    /*
     * Đảm bảo class khởi động chính tồn tại và có thể nạp được.
     */
    @Test
    void applicationClassExists() {
        assertNotNull(VantixApplication.class);
    }
}
