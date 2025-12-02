package com.sagafitmi.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafitmi.ecommerce.dto.OrderDTO;
import com.sagafitmi.ecommerce.mapper.OrderMapper;
import com.sagafitmi.ecommerce.model.CartItem;
import com.sagafitmi.ecommerce.model.Order;
import com.sagafitmi.ecommerce.model.OrderItem;
import com.sagafitmi.ecommerce.model.OrderStatus;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.repository.OrderRepository;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.service.OrderService;
import com.sagafitmi.ecommerce.notification.NotificationManager;
import com.sagafitmi.ecommerce.notification.MailNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private JavaMailSender mailSender;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final NotificationManager notificationManager;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartItemRepository cartItemRepository,
                            UserRepository userRepository,
                            NotificationManager notificationManager) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.notificationManager = notificationManager;
    }

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long userId) {
        if (userId == null) return null;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        if (items == null || items.isEmpty()) return null;

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.NEW)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            // Usar el último precio del producto (historial), no el precio almacenado en el carrito
            BigDecimal price = ci.getProduct() != null && ci.getProduct().getCurrentPriceValue() != null
                ? ci.getProduct().getCurrentPriceValue()
                : BigDecimal.ZERO;
            int qty = ci.getQuantity() != null ? ci.getQuantity() : 0;

            OrderItem oi = OrderItem.builder()
                .order(order)
                .product(ci.getProduct())
                .quantity(qty)
                .price(price)
                .build();

            order.getItems().add(oi);

            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // limpiar carrito
        cartItemRepository.deleteByUserId(userId);

        sendMail(user, saved);

        return OrderMapper.toDTO(saved);
    }

    private void sendMail(User user, Order saved) {
        // Enviar notificación por correo al usuario que realizó la orden
        String recipient = user.getEmail();
        if (recipient != null && !recipient.isBlank()) {
            // Construir referencia con prefijo ODRS-yyMMdd + id
            java.time.LocalDateTime dt = saved.getCreatedAt() != null ? saved.getCreatedAt() : java.time.LocalDateTime.now();
            String yy = String.format("%02d", dt.getYear() % 100);
            String mm = String.format("%02d", dt.getMonthValue());
            String dd = String.format("%02d", dt.getDayOfMonth());
            String orderRef = saved.getId() != null ? ("ODRS-" + yy + mm + dd + "-" + saved.getId()) : "(pendiente)";

            String subject = "Pedido recibido - folio #" + orderRef;

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

            StringBuilder body = new StringBuilder();
            body.append("<html><body style=\"font-family:Arial,Helvetica,sans-serif;color:#333;\">\n");
            body.append("<p>Hola ");
            if (user.getName() != null) body.append(user.getName()).append(" ");
            body.append(",</p>\n");
            body.append("<p>Tu pedido ha sido creado con éxito.</p>\n");
            body.append("<p><strong>Folio de la orden:</strong> ").append(orderRef).append("</p>\n");

            // Tabla con detalle de ítems
            body.append("<table style=\"width:100%;border-collapse:collapse;max-width:700px;\">\n");
            body.append("<thead><tr style=\"background:#f5f5f5;\">\n");
            body.append("<th style=\"padding:8px;border:1px solid #ddd;text-align:left;\">Producto</th>");
            body.append("<th style=\"padding:8px;border:1px solid #ddd;text-align:right;\">Cantidad</th>");
            body.append("<th style=\"padding:8px;border:1px solid #ddd;text-align:right;\">Precio unitario</th>");
            body.append("<th style=\"padding:8px;border:1px solid #ddd;text-align:right;\">Subtotal</th>");
            body.append("</tr></thead><tbody>\n");

            // agregar filas
            java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;
            for (OrderItem oi : saved.getItems()) {
                String prodName = oi.getProduct() != null && oi.getProduct().getName() != null ? oi.getProduct().getName() : "(producto)";
                int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;
                java.math.BigDecimal unit = oi.getPrice() != null ? oi.getPrice() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal subtotal = unit.multiply(java.math.BigDecimal.valueOf(qty));
                grandTotal = grandTotal.add(subtotal);

                body.append("<tr>");
                body.append("<td style=\"padding:8px;border:1px solid #ddd;\">" + escapeHtml(prodName) + "</td>");
                body.append("<td style=\"padding:8px;border:1px solid #ddd;text-align:right;\">" + qty + "</td>");
                body.append("<td style=\"padding:8px;border:1px solid #ddd;text-align:right;\">" + currency.format(unit) + "</td>");
                body.append("<td style=\"padding:8px;border:1px solid #ddd;text-align:right;\">" + currency.format(subtotal) + "</td>");
                body.append("</tr>\n");
            }

            // Totales (usar el que esté en la entidad si existe)
            java.math.BigDecimal totalToShow = saved.getTotal() != null ? saved.getTotal() : grandTotal;

            body.append("</tbody>");
            body.append("<tfoot><tr>");
            body.append("<td colspan=\"3\" style=\"padding:8px;border:1px solid #ddd;text-align:right;font-weight:bold;\">Total</td>");
            body.append("<td style=\"padding:8px;border:1px solid #ddd;text-align:right;font-weight:bold;\">" + currency.format(totalToShow) + "</td>");
            body.append("</tr></tfoot>");
            body.append("</table>\n");

            body.append("<p>Gracias por comprar con nosotros.</p>\n");
            body.append("</body></html>");

            MailNotification mail = new MailNotification(recipient, subject, body.toString());
            mail.setNotificationService(mailSender);
            // Encolamos y enviamos inmediatamente de forma síncrona para asegurar entrega en este flujo
            notificationManager.enqueue(mail);
            try {
                notificationManager.sendAllNowAsync();
            } catch (Exception ex) {
                // Si ocurre algún error en notificación, lo registramos (no revertimos la transacción)
                System.err.println("Error enviando notificación de orden: " + ex.getMessage());
            }
        }
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        if (orderId == null) return null;
        return orderRepository.findById(orderId).map(OrderMapper::toDTO).orElse(null);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(Long userId) {
        if (userId == null) return List.of();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        if (status == null) return List.of();
        return orderRepository.findByStatus(status).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        if (orderId == null || status == null) return null;
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return null;
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return OrderMapper.toDTO(saved);
    }

    @Override
    public Page<OrderDTO> searchOrders(LocalDateTime start, LocalDateTime end, OrderStatus status, int page, int size) {
        Specification<Order> spec = Specification.where(null);

        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), end));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Order> result = orderRepository.findAll(spec, PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt")));
        // Map to summary DTO to avoid initializing `items` collection (lazy)
        return result.map(OrderMapper::toSummaryDTO);
    }
    
    // Pequeña utilidad para escapar nombres/descripciones de productos antes de insertarlos en HTML
    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

}
