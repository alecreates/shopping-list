package edu.uga.cs.shoppinglist;

public class ShoppingItem {

    private String key;
    private String itemName;

    public ShoppingItem() {
        // Required for Firebase
    }

    public ShoppingItem(String itemName) {
        this.itemName = itemName;
    }

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
}