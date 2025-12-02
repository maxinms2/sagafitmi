package com.sagafitmi.ecommerce.notification;

public interface Notification {
    NotificationType getType();
    String getRecipient();
    void setNotificationService(Object service);
    void send() throws Exception;
}
