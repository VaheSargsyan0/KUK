package com.project.kuk;

import com.google.firebase.database.Exclude;
import java.util.List;

public class OrderItem {
    @Exclude
    private String orderId;

    private List<CartItem> items;
    private double totalOrderPrice;
    private String orderDate;
    private String status;

    public OrderItem() {}

    public OrderItem(String orderId, List<CartItem> items, double totalOrderPrice, String orderDate, String status) {
        this.orderId = orderId;
        this.items = items;
        this.totalOrderPrice = totalOrderPrice;
        this.orderDate = orderDate;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public List<CartItem> getItems() { return items; }
    public double getTotalOrderPrice() { return totalOrderPrice; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public void setTotalOrderPrice(double totalOrderPrice) { this.totalOrderPrice = totalOrderPrice; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
}



