package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.from:no-reply@localhost}")
    private String from;

    public void sendRoleRejectionEmail(String to, String fullName, RoleName requestedRole, String reason) {
        if (to == null || to.isBlank()) return;

        String subject = "Your Role Request Has Been Rejected";
        String body = """
            Dear %s,
            
            We regret to inform you that your request to join with the role: %s has been rejected.
            Reason: %s
            
            If you believe this was a mistake, feel free to reply to this email or submit a new request with additional information.
            
            Best regards,
            Insurance System Team
            """.formatted(
                fullName == null ? "" : fullName,
                requestedRole == null ? "-" : requestedRole.name(),
                (reason == null || reason.isBlank()) ? "Not specified" : reason
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try {
            mailSender.send(msg);
        } catch (MailException ignored) {
            // Do not fail the operation
        }
    }


    public void sendRoleApprovalEmail(String to, String fullName, RoleName approvedRole) {
        if (to == null || to.isBlank()) return;

        String subject = "Your Role Request Has Been Approved";
        String body = """
            Dear %s,
            
            We are pleased to inform you that your request to join with the role: %s has been approved.
            You can now log in and start using your new permissions.
            
            Best regards,
            Insurance System Team
            """.formatted(
                fullName == null ? "" : fullName,
                approvedRole == null ? "-" : approvedRole.name()
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try {
            mailSender.send(msg);
        } catch (MailException ignored) {
            // Do not fail the operation
        }


    }

    public void sendCustomEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try {
            mailSender.send(msg);
        } catch (MailException ignored) {
            // ما تخلي الإيميل يفشل العملية
        }
    }
    public void sendSimpleMail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            // ما نفشل العملية حتى لو الإيميل ما مشي
            ex.printStackTrace();
        }
    }

    /**
     * إرسال إيميل خاص بإعادة تعيين كلمة المرور
     */
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        if (to == null || to.isBlank()) return;

        String subject = "Password Reset Request";
        String body = """
                Dear %s,
                
                We received a request to reset your password.
                Please click the link below to reset it:
                
                %s
                
                If you did not request this, you can safely ignore this email.
                
                Best regards,
                Insurance System Team
                """.formatted(fullName == null ? "" : fullName, resetLink);

        sendSimpleMail(to, subject, body);
    }
}


