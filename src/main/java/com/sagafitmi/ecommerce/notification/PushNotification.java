package com.sagafitmi.ecommerce.notification;

public class PushNotification implements Notification {
    private final String deviceToken;
    private final String title;
    private final String body;

    public PushNotification(String deviceToken, String title, String body) {
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

    @Override
    public String getRecipient() {
        return deviceToken;
    }

    @Override
    public void send() throws Exception {
        // Integrar con servicio push real aquí
        System.out.println("[Push] Enviando push a token: " + deviceToken + " title: " + title);
    }

    @Override
    public void setNotificationService(Object service) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNotificationService'");
    }
}
