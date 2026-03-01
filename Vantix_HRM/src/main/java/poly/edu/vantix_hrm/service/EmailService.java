package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(
                    "Password Reset OTP - VANTIX HRM"
            );
            message.setText(
                    "Your OTP code is: " + otp +
                            "\n\nThis code will expire in 5 minutes." +
                            "\n\nDo not share this code with anyone."
            );
            mailSender.send(message);
        }
        catch (Exception e) {
            throw new BusinessException(
                    "email","Failed to send email"
            );
        }

    }

}