package com.sagafitmi.ecommerce.notification;

public interface NotificationListener {
    /**
     * Llamado cuando una notificación ha sido procesada.
     */
    void onNotificationSent(NotificationSender.SendResult result);
}
