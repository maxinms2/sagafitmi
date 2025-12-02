package com.sagafitmi.ecommerce.notification;

public class SmsNotification implements Notification {
    private final String recipientPhone;
    private final String message;

    public SmsNotification(String recipientPhone, String message) {
        this.recipientPhone = recipientPhone;
        this.message = message;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

    @Override
    public String getRecipient() {
        return recipientPhone;
    }

    @Override
    public void send() throws Exception {
        // Integrar con proveedor SMS real aquí
        System.out.println("[SMS] Enviando SMS a: " + recipientPhone + " mensaje: " + message);
    }

    @Override
    public void setNotificationService(Object service) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNotificationService'");
    }
}
