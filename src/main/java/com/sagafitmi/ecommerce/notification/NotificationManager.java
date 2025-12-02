package com.sagafitmi.ecommerce.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

@Service
public class NotificationManager {
    private final NotificationSender sender;
    private final List<Notification> queue = new CopyOnWriteArrayList<>();
    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();

    public NotificationManager(NotificationSender sender) {
        this.sender = sender;
    }

    /**
     * Encola una notificación para su envío posterior.
     */
    public void enqueue(Notification notification) {
        if (notification != null) {
            queue.add(notification);
        }
    }

    /**
     * Devuelve una vista inmodificable de la cola actual.
     */
    public List<Notification> getQueuedNotifications() {
        return Collections.unmodifiableList(new ArrayList<>(queue));
    }

    /**
     * Registra un listener que será notificado tras cada intento de envío.
     */
    public void registerListener(NotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Envía todas las notificaciones encoladas de forma síncrona y vacía la cola.
     */
    public List<NotificationSender.SendResult> sendAllNowSync() {
        List<Notification> toSend = new ArrayList<>(queue);
        queue.removeAll(toSend);
        List<NotificationSender.SendResult> results = sender.sendAllSync(toSend);
        notifyListeners(results);
        return results;
    }

    /**
     * Envía todas las notificaciones encoladas de forma asíncrona y vacía la cola.
     */
    public List<NotificationSender.SendResult> sendAllNowAsync() throws InterruptedException {
        List<Notification> toSend = new ArrayList<>(queue);
        queue.removeAll(toSend);
        List<NotificationSender.SendResult> results = sender.sendAllAsync(toSend);
        notifyListeners(results);
        return results;
    }

    private void notifyListeners(List<NotificationSender.SendResult> results) {
        for (NotificationSender.SendResult r : results) {
            for (NotificationListener l : listeners) {
                try {
                    l.onNotificationSent(r);
                } catch (Exception ex) {
                    // No propagamos excepciones de listeners
                    System.err.println("Error en listener de notificación: " + ex.getMessage());
                }
            }
        }
    }
}
