package org.longg.nh.kickstyleecommerce.domain.services.auth;

import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Value("${app.name:KickStyle E-commerce}")
  private String appName;

  @Value("${app.mail.from}")
  private String fromEmail;

  @Value("${app.mail.from-name}")
  private String fromName;

  @Qualifier("taskExecutor")
  private final TaskExecutor taskExecutor;

  public EmailService(
      JavaMailSender mailSender, @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
    this.mailSender = mailSender;
    this.taskExecutor = taskExecutor;
  }

  public void sendOrderPdfEmail(String toEmail, String fullName, byte[] pdfBytes, String fileName) {
    taskExecutor.execute(
        () -> {
          try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("🧾 Hóa đơn mua hàng - " + appName);
            helper.setText(
                String.format(
                    "Xin chào %s,<br><br>Vui lòng tìm hóa đơn mua hàng của bạn trong file đính kèm.",
                    fullName),
                true);

            helper.addAttachment(fileName, new ByteArrayDataSource(pdfBytes, "application/pdf"));

            mailSender.send(message);
            log.info("Order PDF email sent successfully to: {}", toEmail);
          } catch (Exception e) {
            log.error("Failed to send order PDF email to {}: {}", toEmail, e.getMessage());
          }
        });
  }

  public void sendVerificationEmail(String toEmail, String verificationToken, String fullName) {
    taskExecutor.execute(
        () -> {
          try {
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
            String emailContent = buildVerificationEmailContent(fullName, verificationLink);
            String subject = "Xác thực tài khoản " + appName + " - " + fullName;

            sendHtmlEmail(toEmail, subject, emailContent);

            log.info("Verification email sent successfully to: {}", toEmail);

          } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            // Không throw RuntimeException để tránh làm lỗi thread pool
          }
        });
  }

  /** Send password reset email using SMTP */
  public void sendPasswordResetEmail(String toEmail, String resetToken, String fullName) {
    taskExecutor.execute(
        () -> {
          try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            String emailContent = buildPasswordResetEmailContent(fullName, resetLink);
            String subject = "Đặt lại mật khẩu - " + appName;

            sendHtmlEmail(toEmail, subject, emailContent);

            log.info("Password reset email sent successfully to: {}", toEmail);

          } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            // Không throw RuntimeException để tránh làm lỗi thread pool
          }
        });
  }

  /** Send HTML email via SMTP */
  private void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);

    } catch (MessagingException | UnsupportedEncodingException e) {
      log.error("Failed to send HTML email to: {}", to, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  private String buildVerificationEmailContent(String fullName, String verificationLink) {
    return String.format(
        """
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
                    .button {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white !important;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 25px;
                        margin: 20px 0;
                        font-weight: bold;
                        font-size: 16px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #666;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛍️ %s</h1>
                        <p style="margin: 10px 0 0 0; opacity: 0.9;">Chào mừng bạn đến với cửa hàng bán quần áo bóng đá </p>
                    </div>
                    <div class="content">
                        <h2>🎉 Chào %s!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>%s</strong>!</p>
                        <p>Để hoàn tất quá trình đăng ký và bắt đầu mua sắm những bộ quần áo bóng đá tuyệt vời, vui lòng click vào nút bên dưới để xác thực email:</p>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">✅ Xác thực tài khoản ngay</a>
                        </div>

                        <p><strong>⚠️ Lưu ý:</strong> Link này sẽ hết hạn sau <strong>1 giờ</strong>.</p>
                        <p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>
                    </div>
                    <div class="footer">
                        <p><strong>© 2025 %s</strong></p>
                        <p>Email này được gửi tự động, vui lòng không reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
        appName, fullName, appName, verificationLink, appName);
  }

  private String buildPasswordResetEmailContent(String fullName, String resetLink) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu</title>
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
                        background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%);
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
                    .button {
                        display: inline-block;
                        background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%);
                        color: white;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 25px;
                        margin: 20px 0;
                        font-weight: bold;
                        font-size: 16px;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #666;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 %s</h1>
                        <p style="margin: 10px 0 0 0; opacity: 0.9;">Đặt lại mật khẩu tài khoản</p>
                    </div>
                    <div class="content">
                        <h2>🔑 Chào %s!</h2>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <strong>%s</strong>.</p>
                        <p>Click vào nút bên dưới để đặt lại mật khẩu:</p>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">🔄 Đặt lại mật khẩu</a>
                        </div>

                        <p><strong>⚠️ Lưu ý:</strong> Link này sẽ hết hạn sau <strong>1 giờ</strong>.</p>
                        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và mật khẩu của bạn sẽ không thay đổi.</p>
                    </div>
                    <div class="footer">
                        <p><strong>© 2025 %s</strong></p>
                        <p>Email này được gửi tự động, vui lòng không reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
        appName, fullName, appName, resetLink, appName);
  }
}
