package com.project.kuk;

public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private double totalPrice;
    private String imageUrl;
    private String userId;

    public CartItem() {
    }

    public CartItem(String productId, String productName, double price, int quantity, String imageUrl, String userId) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.totalPrice = price * quantity;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = price * quantity;
    }
}





