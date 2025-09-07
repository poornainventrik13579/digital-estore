package com.inventrik.digitalestore.service.invoice;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.user.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfInvoiceService implements InvoiceService {

    @Override
    public byte[] generateInvoice(Order order, User user) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            addCompanyHeader(document);
            
            addInvoiceDetails(document, order);
            
            addCustomerDetails(document, user);
            
            addItemsTable(document, order);
            
            addTotal(document, order);
            
            addFooter(document);
            
            document.close();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
    
    @Override
    public String storeInvoice(Order order, byte[] invoiceData) {
        
        String invoiceId = "INV-" + order.getOrderId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Storing invoice {} for order {}", invoiceId, order.getOrderId());
        return invoiceId;
    }
    
    @Override
    public byte[] getInvoice(String invoiceId) {
        
        log.info("Retrieving invoice {}", invoiceId);
        throw new UnsupportedOperationException("Invoice retrieval not implemented");
    }
    
    private void addCompanyHeader(Document document) throws DocumentException {
        Paragraph header = new Paragraph();
        header.add(new Paragraph("Digital E-Store", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)));
        header.add(new Paragraph("123 E-Commerce Street", new Font(Font.FontFamily.HELVETICA, 10)));
        header.add(new Paragraph("support@yourdomain.com | www.yourdigitalestore.com", new Font(Font.FontFamily.HELVETICA, 10)));
        header.setSpacingAfter(20);
        document.add(header);
    }
    
    private void addInvoiceDetails(Document document, Order order) throws DocumentException {
        Paragraph invoiceDetails = new Paragraph();
        invoiceDetails.add(new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
        invoiceDetails.add(new Paragraph("Invoice Number: INV-" + order.getOrderId(), new Font(Font.FontFamily.HELVETICA, 10)));
        invoiceDetails.add(new Paragraph("Date: " + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(order.getOrderDate()), new Font(Font.FontFamily.HELVETICA, 10)));
        invoiceDetails.add(new Paragraph("Order ID: " + order.getOrderId(), new Font(Font.FontFamily.HELVETICA, 10)));
        invoiceDetails.setSpacingAfter(20);
        document.add(invoiceDetails);
    }
    
    private void addCustomerDetails(Document document, User user) throws DocumentException {
        Paragraph customerDetails = new Paragraph();
        customerDetails.add(new Paragraph("BILL TO:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        
        String customerName = (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                             (user.getLastName() != null ? user.getLastName() : "");
        if (customerName.trim().isEmpty()) {
            customerName = "Customer";
        }
        
        customerDetails.add(new Paragraph(customerName, new Font(Font.FontFamily.HELVETICA, 10)));
        
        if (user.getUserType() != null && user.getUserType().toString().equals("COMPANY") && user.getCompanyName() != null) {
            customerDetails.add(new Paragraph(user.getCompanyName(), new Font(Font.FontFamily.HELVETICA, 10)));
            
            if (user.getCompanyAddress1() != null) {
                customerDetails.add(new Paragraph(user.getCompanyAddress1(), new Font(Font.FontFamily.HELVETICA, 10)));
            }
            
            if (user.getCompanyAddress2() != null) {
                customerDetails.add(new Paragraph(user.getCompanyAddress2(), new Font(Font.FontFamily.HELVETICA, 10)));
            }
            
            if (user.getCompanyCountry() != null) {
                customerDetails.add(new Paragraph(user.getCompanyCountry(), new Font(Font.FontFamily.HELVETICA, 10)));
            }
            
            if (user.getTaxId() != null) {
                customerDetails.add(new Paragraph("Tax ID: " + user.getTaxId(), new Font(Font.FontFamily.HELVETICA, 10)));
            }
        }
        
        customerDetails.add(new Paragraph("Email: " + user.getEmail(), new Font(Font.FontFamily.HELVETICA, 10)));
        customerDetails.setSpacingAfter(20);
        document.add(customerDetails);
    }
    
    private void addItemsTable(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{6, 2, 2});
        
        PdfPCell cell = new PdfPCell(new Phrase("Description", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Price", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Amount", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        table.addCell(cell);
        
        Font itemFont = new Font(Font.FontFamily.HELVETICA, 10);
        
        for (int i = 0; i < order.getOrderItems().size(); i++) {
            var item = order.getOrderItems().get(i);
            
            cell = new PdfPCell(new Phrase("Digital Product ID: " + item.getProductId(), itemFont));
            cell.setPadding(5);
            table.addCell(cell);
            
            String formattedPrice = formatCurrency(item.getPriceAtPurchase(), order.getCurrency());
            cell = new PdfPCell(new Phrase(formattedPrice, itemFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(5);
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(formattedPrice, itemFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        document.add(table);
    }
    
    private void addTotal(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1, 1});
        table.setSpacingBefore(10);
        
        PdfPCell cell = new PdfPCell(new Phrase("Subtotal:", new Font(Font.FontFamily.HELVETICA, 10)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase(formatCurrency(order.getTotalAmount(), order.getCurrency()), 
                new Font(Font.FontFamily.HELVETICA, 10)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Total:", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase(formatCurrency(order.getTotalAmount(), order.getCurrency()), 
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        
        document.add(table);
    }
    
    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(Chunk.NEWLINE);
        footer.add(Chunk.NEWLINE);
        footer.add(new Paragraph("Thank you for your business!", new Font(Font.FontFamily.HELVETICA, 10)));
        footer.add(new Paragraph("This is a digital invoice for digital products. No physical shipment will be made.", 
                new Font(Font.FontFamily.HELVETICA, 10)));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
    
    private String formatCurrency(BigDecimal amount, String currency) {
        return String.format("%s %.2f", currency, amount);
    }
}