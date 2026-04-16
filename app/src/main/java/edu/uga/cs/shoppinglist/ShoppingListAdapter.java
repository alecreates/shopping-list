package edu.uga.cs.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder> {

    private List<ShoppingItem> shoppingItemList;
    private OnPurchasedClickListener listener;

    public interface OnPurchasedClickListener {
        void onPurchasedClick(ShoppingItem item);
    }

    public ShoppingListAdapter(List<ShoppingItem> shoppingItemList, OnPurchasedClickListener listener) {
        this.shoppingItemList = shoppingItemList;
        this.listener = listener;
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
        
        if (item.isPurchased()) {
            holder.purchasedButton.setVisibility(View.GONE);
        } else {
            holder.purchasedButton.setVisibility(View.VISIBLE);
            holder.purchasedButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPurchasedClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        Button purchasedButton;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemName);
            purchasedButton = itemView.findViewById(R.id.purchasedButton);
        }
    }
}