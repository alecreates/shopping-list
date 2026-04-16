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

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private static final String TAG = "ShoppingListFragment";
    private EditText addItemEditText;
    private Button addItemButton;
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> shoppingItemList;
    private DatabaseReference databaseReference;

    public ShoppingListFragment() {
        super(R.layout.fragment_shopping_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        addItemEditText = view.findViewById(R.id.addItemEditText);
        addItemButton = view.findViewById(R.id.addItemButton);
        recyclerView = view.findViewById(R.id.recyclerView);

        shoppingItemList = new ArrayList<>();
        adapter = new ShoppingListAdapter(shoppingItemList, item -> {
            markItemAsPurchased(item);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("shopping_items");

        addItemButton.setOnClickListener(v -> {
            String itemName = addItemEditText.getText().toString().trim();
            if (!itemName.isEmpty()) {
                addItemToDatabase(itemName);
            } else {
                Toast.makeText(getContext(), "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        loadShoppingItems();

        return view;
    }

    private void addItemToDatabase(String itemName) {
        String key = databaseReference.push().getKey();
        ShoppingItem item = new ShoppingItem(itemName);
        item.setKey(key);

        if (key != null) {
            databaseReference.child(key).setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        addItemEditText.setText("");
                        Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add item", e));
        }
    }

    private void markItemAsPurchased(ShoppingItem item) {
        if (item.getKey() != null) {
            databaseReference.child(item.getKey()).child("purchased").setValue(true)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Item marked as purchased", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to mark item as purchased", e));
        }
    }

    private void loadShoppingItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingItemList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ShoppingItem item = postSnapshot.getValue(ShoppingItem.class);
                    if (item != null && !item.isPurchased()) {
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