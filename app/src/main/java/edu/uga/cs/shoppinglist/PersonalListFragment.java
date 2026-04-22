package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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
        super(R.layout.fragment_purchased_list); // Reuse the same layout as purchased list
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchased_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        personalItemList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ShoppingListAdapter(
                personalItemList,
                new ShoppingListAdapter.OnItemActionListener() {
                    @Override
                    public void onPurchasedClick(ShoppingItem item) {
                        markItemAsPurchased(item);
                    }

                    @Override
                    public void onEditClick(ShoppingItem item) {}

                    @Override
                    public void onDeleteClick(ShoppingItem item) {}
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

    private void markItemAsPurchased(ShoppingItem item) {
        if (item.getKey() == null) return;

        String originalKey = item.getKey();
        String newKey = purchasedReference.push().getKey();

        ShoppingItem purchasedItem = new ShoppingItem(item.getItemName());
        purchasedItem.setKey(newKey);

        purchasedReference.child(newKey).setValue(purchasedItem)
                .addOnSuccessListener(aVoid -> {
                    shoppingReference.child(originalKey).removeValue()
                            .addOnSuccessListener(v ->
                                    Toast.makeText(getContext(), "Item purchased!", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to purchase item", e));
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