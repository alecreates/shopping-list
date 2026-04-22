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

import java.util.ArrayList;
import java.util.List;

public class PurchasedListFragment extends Fragment {

    private static final String TAG = "PurchasedListFragment";
    private RecyclerView recyclerView;
    private TextView totalPriceTextView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> purchasedItemList;
    private DatabaseReference purchasedReference;

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
        adapter = new ShoppingListAdapter(purchasedItemList, null, ShoppingListAdapter.ListMode.PURCHASED);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");

        loadPurchasedItems();

        return view;
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