package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PersonalListFragment extends Fragment {

    private static final String TAG = "PersonalListFragment";
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> personalItemList;
    private DatabaseReference shoppingReference;
    private DatabaseReference purchasedReference;
    private String currentUserId;

    public PersonalListFragment() {
        super(R.layout.fragment_personal_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        personalItemList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ShoppingListAdapter(
                personalItemList,
                new ShoppingListAdapter.OnItemActionListener() {
                    @Override
                    public void onPurchasedClick(ShoppingItem item) {
                        showPriceInputDialog(item);
                    }

                    @Override
                    public void onEditClick(ShoppingItem item) {}

                    @Override
                    public void onDeleteClick(ShoppingItem item) { showMoveConfirmationDialog(item); }
                },
                ShoppingListAdapter.ListMode.PERSONAL
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        shoppingReference = FirebaseDatabase.getInstance().getReference("shopping_items");
        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");

        loadPersonalItems();

        return view;
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

        String originalKey = item.getKey();

        ShoppingItem movedItem = new ShoppingItem(item.getItemName());
        movedItem.setKey(originalKey);      // keep same key
        movedItem.setPrice(0.0);
        movedItem.setShopperId(null);       // unassign from user

        shoppingReference.child(originalKey).setValue(movedItem)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(),
                                "Returned to shopping list",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to move item back", e)
                );
    }

    private void showPriceInputDialog(ShoppingItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Price for " + item.getItemName());

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("0.00");
        builder.setView(input);

        builder.setPositiveButton("Purchase", (dialog, which) -> {
            String priceStr = input.getText().toString();
            double price = 0.0;
            if (!priceStr.isEmpty()) {
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            markItemAsPurchased(item, price);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void markItemAsPurchased(ShoppingItem item, double price) {
        if (item.getKey() == null) return;

        String originalKey = item.getKey();
        String newKey = purchasedReference.push().getKey();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String displayName = "Unknown User";
        if (user != null && user.getDisplayName() != null) {
            displayName = user.getDisplayName();
        }

        ShoppingItem purchasedItem = new ShoppingItem(item.getItemName());
        purchasedItem.setKey(newKey);
        purchasedItem.setPrice(price);
        purchasedItem.setShopperId(currentUserId);
        purchasedItem.setShopperName(displayName);
        purchasedItem.setPurchasedDate(System.currentTimeMillis());

        purchasedReference.child(newKey).setValue(purchasedItem)
                .addOnSuccessListener(aVoid -> {
                    shoppingReference.child(originalKey).removeValue()
                            .addOnSuccessListener(v ->
                                    Toast.makeText(getContext(),
                                            "Item purchased!",
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to purchase item", e));
    }

    private void loadPersonalItems() {
        shoppingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personalItemList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ShoppingItem item = postSnapshot.getValue(ShoppingItem.class);
                    if (item != null && currentUserId.equals(item.getShopperId())) {
                        personalItemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load personal items", error.toException());
            }
        });
    }
}