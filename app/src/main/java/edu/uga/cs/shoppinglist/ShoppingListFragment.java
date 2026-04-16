package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.AlertDialog;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private static final String TAG = "ShoppingListFragment";
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> shoppingItemList;
    private DatabaseReference shoppingReference;
    private DatabaseReference purchasedReference;
    private FloatingActionButton fabAddItem;

    public ShoppingListFragment() {
        super(R.layout.fragment_shopping_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAddItem = view.findViewById(R.id.fabAddItem);

        shoppingItemList = new ArrayList<>();
        adapter = new ShoppingListAdapter(shoppingItemList, item -> {
            markItemAsPurchased(item);
        }, false);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        shoppingReference = FirebaseDatabase.getInstance().getReference("shopping_items");
        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");

        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        loadShoppingItems();

        return view;
    }

    private void showAddItemDialog() {
        EditText input = new EditText(getContext());
        input.setHint("Enter item name");

        new AlertDialog.Builder(getContext())
                .setTitle("Add Item")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemName = input.getText().toString().trim();

                    if (!itemName.isEmpty()) {
                        addItemToDatabase(itemName);
                    } else {
                        Toast.makeText(getContext(),
                                "Item name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addItemToDatabase(String itemName) {
        String key = shoppingReference.push().getKey();
        ShoppingItem item = new ShoppingItem(itemName);
        item.setKey(key);

        if (key != null) {
            shoppingReference.child(key).setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add item", e));
        }
    }

    private void markItemAsPurchased(ShoppingItem item) {

        if (item.getKey() == null) return;

        String originalKey = item.getKey();
        String newKey = purchasedReference.push().getKey();

        // Copy item into purchased list
        ShoppingItem purchasedItem = new ShoppingItem(item.getItemName());

        purchasedItem.setKey(newKey);

        // 1. Add to purchased_items
        purchasedReference.child(newKey).setValue(purchasedItem)
                .addOnSuccessListener(aVoid -> {

                    // 2. Remove from shopping list
                    shoppingReference.child(originalKey).removeValue()
                            .addOnSuccessListener(v ->
                                    Toast.makeText(getContext(),
                                            "Moved to purchased items",
                                            Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to remove from shopping list", e)
                            );

                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to add to purchased items", e)
                );
    }

    private void loadShoppingItems() {
        shoppingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingItemList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ShoppingItem item = postSnapshot.getValue(ShoppingItem.class);
                    if (item != null) {
                        shoppingItemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load items", error.toException());
            }
        });
    }
}