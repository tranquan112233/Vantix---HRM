package poly.edu.vantix_hrm.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableAsync
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔐 [Vantix HRM] Your OTP Code");

            String html = buildOtpTemplate(otp);

            helper.setText(html, true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpTemplate(String otp) {
        return """
    <!DOCTYPE html>
    <html>
    <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
    
        <table width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
            <tr>
                <td align="center">

                    <!-- CARD -->
                    <table width="420" cellpadding="0" cellspacing="0"
                           style="background:#ffffff;border-radius:14px;
                           padding:32px 28px;text-align:center;
                           box-shadow:0 6px 24px rgba(0,0,0,0.06);">

                        <!-- BRAND -->
                        <tr>
                            <td style="font-size:18px;font-weight:700;color:#6366f1;padding-bottom:12px;">
                                Vantix HRM
                            </td>
                        </tr>

                        <!-- TITLE -->
                        <tr>
                            <td style="font-size:20px;font-weight:700;color:#0f172a;padding-bottom:8px;">
                                OTP Verification
                            </td>
                        </tr>

                        <!-- SUB TEXT -->
                        <tr>
                            <td style="font-size:14px;color:#64748b;padding-bottom:20px;">
                                Enter the verification code below to continue
                            </td>
                        </tr>

                        <!-- OTP BOX -->
                        <tr>
                            <td align="center">
                                <div style="
                                    display:inline-block;
                                    background:#f8fafc;
                                    border:1px solid #e2e8f0;
                                    border-radius:10px;
                                    padding:14px 22px;
                                    font-size:28px;
                                    font-weight:700;
                                    letter-spacing:6px;
                                    color:#6366f1;
                                ">
                                    %s
                                </div>
                            </td>
                        </tr>

                        <!-- INFO -->
                        <tr>
                            <td style="font-size:13px;color:#64748b;padding-top:18px;line-height:1.5;">
                                This code will expire in <b>5 minutes</b>.<br/>
                                Please do not share this code with anyone.
                            </td>
                        </tr>

                        <!-- DIVIDER -->
                        <tr>
                            <td style="padding:20px 0;">
                                <div style="height:1px;background:#e2e8f0;"></div>
                            </td>
                        </tr>

                        <!-- FOOTER -->
                        <tr>
                            <td style="font-size:12px;color:#94a3b8;line-height:1.5;">
                                If you didn’t request this email, you can safely ignore it.<br/>
                                © 2026 Vantix HRM. All rights reserved.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """.formatted(otp);
    }
}