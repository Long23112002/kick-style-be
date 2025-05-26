package org.longg.nh.kickstyleecommerce.domain.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.services.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String verificationToken) {
        String subject = "Xác thực tài khoản KickStyle";
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        
        String htmlContent = buildVerificationEmailTemplate(verificationUrl);
        
        sendHtmlMessage(to, subject, htmlContent);
        log.info("Verification email sent to: {}", to);
    }

    private String buildVerificationEmailTemplate(String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác thực tài khoản</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .content h2 {
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .button {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 25px;
                        margin: 20px 0;
                        font-weight: bold;
                        font-size: 16px;
                        transition: transform 0.3s ease;
                    }
                    .button:hover {
                        transform: translateY(-2px);
                    }
                    .url-box {
                        word-break: break-all;
                        background-color: #f8f9fa;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #667eea;
                        margin: 20px 0;
                        font-family: monospace;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #666;
                        border-top: 1px solid #eee;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        color: #856404;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛍️ KickStyle E-commerce</h1>
                        <p style="margin: 10px 0 0 0; opacity: 0.9;">Chào mừng bạn đến với cửa hàng giày thể thao hàng đầu!</p>
                    </div>
                    <div class="content">
                        <h2>🎉 Xác thực tài khoản của bạn</h2>
                        <p>Chào mừng bạn đến với <strong>KickStyle</strong>!</p>
                        <p>Cảm ơn bạn đã đăng ký tài khoản. Để hoàn tất việc đăng ký và bắt đầu mua sắm những đôi giày thể thao tuyệt vời, vui lòng nhấp vào nút bên dưới để xác thực email của bạn:</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">✅ Xác thực tài khoản ngay</a>
                        </div>
                        
                        <p>Hoặc bạn có thể copy và paste đường link sau vào trình duyệt:</p>
                        <div class="url-box">%s</div>
                        
                        <div class="warning">
                            <strong>⚠️ Lưu ý quan trọng:</strong>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>Link xác thực này sẽ hết hạn sau <strong>1 giờ</strong></li>
                                <li>Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này</li>
                                <li>Không chia sẻ link này với bất kỳ ai khác</li>
                            </ul>
                        </div>
                        
                        <p>Sau khi xác thực thành công, bạn sẽ có thể:</p>
                        <ul>
                            <li>🛒 Mua sắm các sản phẩm giày thể thao chính hãng</li>
                            <li>💝 Nhận thông báo về các chương trình khuyến mãi đặc biệt</li>
                            <li>📦 Theo dõi đơn hàng của bạn</li>
                            <li>⭐ Đánh giá và review sản phẩm</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p><strong>© 2025 KickStyle E-commerce</strong></p>
                        <p>Email này được gửi tự động, vui lòng không reply.</p>
                        <p>Nếu bạn cần hỗ trợ, vui lòng liên hệ: support@kickstyle.com</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationUrl, verificationUrl);
    }
} 