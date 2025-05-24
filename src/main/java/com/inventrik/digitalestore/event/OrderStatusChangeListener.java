package com.inventrik.digitalestore.event;

// import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.service.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangeListener {

    private final EmailNotificationService emailNotificationService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChangeEvent(OrderStatusChangeEvent event) {
        log.info("Processing order status change event: Order {} changed from {} to {}", 
                event.getOrder().getOrderId(), event.getOldStatus(), event.getNewStatus());
        
        emailNotificationService.handleOrderStatusChange(
                event.getOrder(), 
                event.getOldStatus(), 
                event.getNewStatus());
    }
}