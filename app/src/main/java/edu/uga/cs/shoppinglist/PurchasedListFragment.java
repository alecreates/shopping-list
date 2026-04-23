package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class PurchasedListFragment extends Fragment {

    private static final String TAG = "PurchasedListFragment";
    private RecyclerView recyclerView;
    private TextView totalPriceTextView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> purchasedItemList;
    private DatabaseReference purchasedReference;
    private DatabaseReference shoppingReference;

    public PurchasedListFragment() {
        super(R.layout.fragment_purchased_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_purchased_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        totalPriceTextView = view.findViewById(R.id.totalPrice);

        purchasedItemList = new ArrayList<>();
        adapter = new ShoppingListAdapter(purchasedItemList,
                new ShoppingListAdapter.OnItemActionListener() {
                    @Override
                    public void onPurchasedClick(ShoppingItem item) {}

                    @Override
                    public void onEditClick(ShoppingItem item) { showEditPriceDialog(item); }

                    @Override
                    public void onDeleteClick(ShoppingItem item) { showMoveConfirmationDialog(item); }
                },
                ShoppingListAdapter.ListMode.PURCHASED);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");
        shoppingReference = FirebaseDatabase.getInstance().getReference("shopping_items");

        loadPurchasedItems();

        return view;
    }


    private void showEditPriceDialog(ShoppingItem item) {
        if (getContext() == null || item == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Price");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);

        input.setHint("0.00");
        input.setText(String.format(java.util.Locale.US, "%.2f", item.getPrice()));
        input.setSelection(input.getText().length());

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String priceText = input.getText().toString().trim();

            if (priceText.isEmpty()) {
                Toast.makeText(getContext(),
                        "Enter a valid price",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newPrice = Double.parseDouble(priceText);
                updateItemPrice(item, newPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(),
                        "Invalid price format",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateItemPrice(ShoppingItem item, double newPrice) {
        if (item.getKey() == null) return;

        purchasedReference.child(item.getKey())
                .child("price")
                .setValue(newPrice)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(),
                                "Price updated",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update price", e));
    }

    private void showMoveConfirmationDialog(ShoppingItem item) {

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Move Item")
                .setMessage("Are you sure you want to move \"" + item.getItemName() + "\" back to the shopping list?")
                .setPositiveButton("Move", (dialog, which) -> {
                    moveItemToShoppingList(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void moveItemToShoppingList(ShoppingItem item) {
        if (item.getKey() == null) return;

        String purchasedKey = item.getKey();
        String newShoppingKey = shoppingReference.push().getKey();

        ShoppingItem movedItem = new ShoppingItem(item.getItemName());
        movedItem.setKey(newShoppingKey);
        movedItem.setPrice(0.0);
        movedItem.setShopperId(null);

        shoppingReference.child(newShoppingKey).setValue(movedItem)
                .addOnSuccessListener(aVoid -> {

                    purchasedReference.child(purchasedKey).removeValue()
                            .addOnSuccessListener(v ->
                                    android.widget.Toast.makeText(
                                            getContext(),
                                            "Moved back to shopping list",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to move item", e)
                );
    }


    private void loadPurchasedItems() {
        purchasedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                purchasedItemList.clear();
                double total = 0.0;

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ShoppingItem item = postSnapshot.getValue(ShoppingItem.class);
                    if (item != null) {
                        purchasedItemList.add(item);
                        total += item.getPrice();
                    }
                }

                adapter.notifyDataSetChanged();
                totalPriceTextView.setText(String.format(java.util.Locale.US, "Total: $%.2f", total));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load purchased items", error.toException());
            }
        });
    }
}