package poly.edu.vantix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.exception.BusinessException;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:no-reply@vantix.local}")
    private String fromAddress;

    @Value("${app.mail.from-name:Vantix}")
    private String fromName;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void sendPasswordResetOtp(User user, String otp, int expiresInMinutes) {
        if (!enabled) {
            return;
        }
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            throw new BusinessException("email", "User email is not configured");
        }

        String subject = "Mã OTP đặt lại mật khẩu Vantix";
        String body = """
                Xin chào %s,

                Mã OTP đặt lại mật khẩu Vantix của bạn là: %s
                Mã này có hiệu lực trong %d phút.

                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.

                %s
                """.formatted(
                StringUtils.hasText(user.getUsername()) ? user.getUsername() : user.getEmail(),
                otp,
                expiresInMinutes,
                fromName
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException("Unable to send OTP email. Please check SMTP configuration.");
        }
    }
}
