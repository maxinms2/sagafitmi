package com.sagafitmi.ecommerce.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class NotificationSender {
    private final ExecutorService executor;

    public NotificationSender() {
        // pequeño pool para envío concurrente; ajustar según necesidad
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Envía todas las notificaciones de manera síncrona (cada send() secuencial).
     */
    public List<SendResult> sendAllSync(List<Notification> notifications) {
        List<SendResult> results = new ArrayList<>();
        for (Notification n : notifications) {
            try {
                n.send();
                results.add(new SendResult(n, true, null));
            } catch (Exception e) {
                results.add(new SendResult(n, false, e));
            }
        }
        return results;
    }

    /**
     * Envía todas las notificaciones en paralelo usando un pool.
     */
    public List<SendResult> sendAllAsync(List<Notification> notifications) throws InterruptedException {
        List<Callable<SendResult>> tasks = new ArrayList<>();
        for (Notification n : notifications) {
            tasks.add(() -> {
                try {
                    n.send();
                    log.info("Notificación enviada: {}", n.getRecipient());
                    return new SendResult(n, true, null);
                    
                } catch (Exception e) {
                    log.error("Error enviando notificación a {}: {}", n.getRecipient(), e.getMessage());
                    return new SendResult(n, false, e);
                }
            });
        }

        try {
            List<Future<SendResult>> futures = executor.invokeAll(tasks);
            List<SendResult> results = new ArrayList<>();
            for (Future<SendResult> f : futures) {
                try {
                    results.add(f.get());
                } catch (Exception e) {
                    // No debería pasar, pero capturamos y añadimos un resultado de fallo genérico
                    results.add(new SendResult(null, false, e));
                }
            }
            return results;
        } finally {
            // no cerramos el executor aquí; el consumidor decide cuándo apagarlo
        }
    }

    public static class SendResult {
        private final Notification notification;
        private final boolean success;
        private final Exception error;

        public SendResult(Notification notification, boolean success, Exception error) {
            this.notification = notification;
            this.success = success;
            this.error = error;
        }

        public Notification getNotification() {
            return notification;
        }

        public boolean isSuccess() {
            return success;
        }

        public Exception getError() {
            return error;
        }
    }
}
