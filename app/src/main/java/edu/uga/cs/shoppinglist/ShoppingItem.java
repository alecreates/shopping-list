package edu.uga.cs.shoppinglist;

public class ShoppingItem {
    private String key;
    private String itemName;
    private boolean isPurchased;

    public ShoppingItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingItem.class)
    }

    public ShoppingItem(String itemName) {
        this.itemName = itemName;
        this.isPurchased = false;
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

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }
}