package com.inventrik.digitalestore.service.invoice;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.user.User;

public interface InvoiceService {
    
    byte[] generateInvoice(Order order, User user);
    
    String storeInvoice(Order order, byte[] invoiceData);
    
    byte[] getInvoice(String invoiceId);
}