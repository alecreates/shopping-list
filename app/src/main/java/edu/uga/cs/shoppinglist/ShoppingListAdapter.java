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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_item_row, parent, false);
        return new ShoppingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        ShoppingItem item = shoppingItemList.get(position);
        holder.itemNameTextView.setText(item.getItemName());

        if (mode == ListMode.PURCHASED) {
            // hide all buttons in purchased list
            holder.purchasedButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else if (mode == ListMode.PERSONAL) {
            // Personal list: only show purchased button (which actually marks as purchased)
            // Edit/Delete are hidden as per requirements
            holder.purchasedButton.setVisibility(View.VISIBLE);
            holder.purchasedButton.setImageResource(android.R.drawable.checkbox_on_background); // Change icon to indicate "complete"
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);

            holder.purchasedButton.setOnClickListener(v -> {
                if (listener != null) listener.onPurchasedClick(item);
            });
        } else {
            // Default shopping list: show all
            holder.purchasedButton.setVisibility(View.VISIBLE);
            holder.purchasedButton.setImageResource(R.drawable.ic_outline_add_shopping_cart_24);
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.purchasedButton.setOnClickListener(v -> {
                if (listener != null) listener.onPurchasedClick(item);
            });

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(item);
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        ImageButton purchasedButton;
        ImageButton editButton;
        ImageButton deleteButton;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemName);
            purchasedButton = itemView.findViewById(R.id.purchasedButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}