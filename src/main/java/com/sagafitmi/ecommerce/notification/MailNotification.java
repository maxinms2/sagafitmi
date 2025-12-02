package com.sagafitmi.ecommerce.notification;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

public class MailNotification implements Notification {

    private final String recipient;
    private final String subject;
    private final String body;
    private JavaMailSender mailSender;

    public MailNotification(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.MAIL;
    }

    @Override
    public String getRecipient() {
        return recipient;
    }

    @Override
    public void send() throws Exception {
        // Integrar con servicio de correo real aquí
        System.out.println("[Mail] Enviando correo a: " + recipient + " asunto: " + subject);
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom("sagafitmi@gmail.com", "SAGAFITMI");
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(body, true); // true => HTML
        mailSender.send(mime);

    }

    @Override
    public void setNotificationService(Object service) {
        this.mailSender = (JavaMailSender) service;
    }
}
