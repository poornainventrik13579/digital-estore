package com.inventrik.digitalestore.service.invoice;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.user.User;

public interface InvoiceService {
    
    /**
     * Generate an invoice PDF for an order
     * 
     * @param order The order to generate an invoice for
     * @param user The user who placed the order
     * @return The invoice PDF as a byte array
     */
    byte[] generateInvoice(Order order, User user);
    
    /**
     * Store an invoice in the system
     * 
     * @param order The order associated with the invoice
     * @param invoiceData The PDF invoice data
     * @return The invoice ID
     */
    String storeInvoice(Order order, byte[] invoiceData);
    
    /**
     * Retrieve a stored invoice
     * 
     * @param invoiceId The invoice ID
     * @return The invoice PDF as a byte array
     */
    byte[] getInvoice(String invoiceId);
}