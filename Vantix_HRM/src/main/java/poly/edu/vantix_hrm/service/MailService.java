package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    public void sendResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Vantix HRM - Mã đặt lại mật khẩu");
            msg.setText("Mã xác nhận của bạn là: " + code + "\nMã có hiệu lực trong 10 phút.");
            mailSender.send(msg);
        } catch (Exception e) {
            // fallback để bạn test khi chưa cấu hình SMTP
            log.warn("Không gửi được email (SMTP chưa cấu hình?). Code reset cho {}: {}", toEmail, code, e);
        }
    }
}
