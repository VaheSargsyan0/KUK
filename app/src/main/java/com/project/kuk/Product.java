package com.project.kuk;

public class Product {
    private String productId;
    private String productName;
    private String description;
    private String imageUrl;
    private double price;

    public Product() {
    }

    public Product(String productId, String productName, String description, String imageUrl, double price) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
}
