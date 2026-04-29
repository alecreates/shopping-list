package edu.uga.cs.shoppinglist;

/**
 * ShoppingItem represents a single item in the shopping application.
 *
 * It is used to store item data in Firebase Realtime Database and track:
 * - Item identity (key, name)
 * - Assignment to a user (shopperId, shopperName)
 * - Purchase details (price, purchasedDate)
 */
public class ShoppingItem {

    private String key;
    private String itemName;
    private String shopperId;
    private double price;
    private String shopperName;
    private long purchasedDate;

    public ShoppingItem() {
        // Required for Firebase
    }

    /**
     * Constructs a new ShoppingItem with only a name.
     *
     * Default values are used for all other fields.
     *
     * @param itemName name of the item
     */
    public ShoppingItem(String itemName) {
        this.itemName = itemName;
        this.shopperId = null;
        this.price = 0.0;
        this.shopperName = null;
        this.purchasedDate = 0;
    }

    // getters and setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getShopperId() {
        return shopperId;
    }

    public void setShopperId(String shopperId) {
        this.shopperId = shopperId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShopperName() {
        return shopperName;
    }

    public void setShopperName(String shopperName) {
        this.shopperName = shopperName;
    }

    public long getPurchasedDate() {
        return purchasedDate;
    }

    public void setPurchasedDate(long purchasedDate) {
        this.purchasedDate = purchasedDate;
    }
}