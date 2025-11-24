
package com.inventrik.digitalestore.event;

import com.inventrik.digitalestore.domain.order.Order;
import lombok.Getter;

@Getter
public class OrderStatusChangeEvent {
    private final Order order;
    private final String oldStatus;
    private final String newStatus;
    
    public OrderStatusChangeEvent(Order order, String oldStatus, String newStatus) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}