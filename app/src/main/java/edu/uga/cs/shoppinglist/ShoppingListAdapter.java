package edu.uga.cs.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder> {

    private List<ShoppingItem> shoppingItemList;
    private OnItemActionListener listener;
    public enum ListMode { SHOPPING, PERSONAL, PURCHASED }
    private ListMode mode;

    public interface OnItemActionListener {
        void onPurchasedClick(ShoppingItem item);
        void onEditClick(ShoppingItem item);
        void onDeleteClick(ShoppingItem item);
    }
    public ShoppingListAdapter(List<ShoppingItem> shoppingItemList,
                               OnItemActionListener listener,
                               ListMode mode) {
        this.shoppingItemList = shoppingItemList;
        this.listener = listener;
        this.mode = mode;
    }
    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (mode == ListMode.PURCHASED) ? R.layout.purchased_item_row : R.layout.shopping_item_row;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ShoppingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        ShoppingItem item = shoppingItemList.get(position);
        holder.itemNameTextView.setText(item.getItemName());

        if (mode == ListMode.PURCHASED) {
            // hide all buttons in purchased list if they exist in the layout
            if (holder.purchasedButton != null) holder.purchasedButton.setVisibility(View.GONE);
            if (holder.editButton != null) holder.editButton.setVisibility(View.GONE);
            if (holder.deleteButton != null) holder.deleteButton.setVisibility(View.GONE);

            // Display price for purchased items
            if (holder.itemPriceTextView != null) {
                holder.itemPriceTextView.setVisibility(View.VISIBLE);
                holder.itemPriceTextView.setText(String.format(Locale.US, "$%.2f", item.getPrice()));
            }
        } else if (mode == ListMode.PERSONAL) {
            // Personal list: only show purchased button (which actually marks as purchased)
            // Edit/Delete are hidden as per requirements
            if (holder.purchasedButton != null) {
                holder.purchasedButton.setVisibility(View.VISIBLE);
                holder.purchasedButton.setImageResource(android.R.drawable.checkbox_on_background); // Change icon to indicate "complete"
                holder.purchasedButton.setOnClickListener(v -> {
                    if (listener != null) listener.onPurchasedClick(item);
                });
            }
            if (holder.editButton != null) holder.editButton.setVisibility(View.GONE);
            if (holder.deleteButton != null) holder.deleteButton.setVisibility(View.GONE);
        } else {
            // Default shopping list: show all
            if (holder.purchasedButton != null) {
                holder.purchasedButton.setVisibility(View.VISIBLE);
                holder.purchasedButton.setImageResource(R.drawable.ic_outline_add_shopping_cart_24);
                holder.purchasedButton.setOnClickListener(v -> {
                    if (listener != null) listener.onPurchasedClick(item);
                });
            }

            if (holder.editButton != null) {
                holder.editButton.setVisibility(View.VISIBLE);
                holder.editButton.setOnClickListener(v -> {
                    if (listener != null) listener.onEditClick(item);
                });
            }

            if (holder.deleteButton != null) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemPriceTextView;
        ImageButton purchasedButton;
        ImageButton editButton;
        ImageButton deleteButton;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemName);
            itemPriceTextView = itemView.findViewById(R.id.itemPrice);
            purchasedButton = itemView.findViewById(R.id.purchasedButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}