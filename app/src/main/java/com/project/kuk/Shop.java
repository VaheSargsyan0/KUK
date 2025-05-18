package com.project.kuk;

public class Shop {
    private String shopId;
    private String shopName;
    private String description;
    private String logoUrl;
    private String ownerId;

    public Shop() {
    }

    public Shop(String shopId, String shopName, String description, String logoUrl, String ownerId) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.description = description;
        this.logoUrl = logoUrl;
        this.ownerId = ownerId;
    }

    public String getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getDescription() { return description; }
    public String getLogoUrl() { return logoUrl; }
    public String getOwnerId() { return ownerId; }
}

