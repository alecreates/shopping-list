package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private static final String TAG = "SummaryFragment";

    private TextView totalSpentTextView;
    private TextView averageSpentTextView;
    private RecyclerView roommateSummaryRecyclerView;
    private Button settleButton;

    private DatabaseReference purchasedReference;
    private List<RoommateSummary> roommateSummaries;
    private RoommateSummaryAdapter adapter;

    public SummaryFragment() {
        super(R.layout.fragment_summary);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        totalSpentTextView = view.findViewById(R.id.totalSpentTextView);
        averageSpentTextView = view.findViewById(R.id.averageSpentTextView);
        roommateSummaryRecyclerView = view.findViewById(R.id.roommateSummaryRecyclerView);
        settleButton = view.findViewById(R.id.settleButton);

        roommateSummaries = new ArrayList<>();
        adapter = new RoommateSummaryAdapter(roommateSummaries);
        roommateSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roommateSummaryRecyclerView.setAdapter(adapter);

        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");

        settleButton.setOnClickListener(v -> showSettleConfirmationDialog());

        loadSummaryData();

        return view;
    }

    private void loadSummaryData() {
        purchasedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Double> spentPerRoommate = new HashMap<>();
                double totalSpent = 0.0;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ShoppingItem item = itemSnapshot.getValue(ShoppingItem.class);
                    if (item != null) {
                        String shopperName = item.getShopperName();
                        if (shopperName == null || shopperName.isEmpty()) {
                            shopperName = "Unknown Roommate";
                        }
                        double price = item.getPrice();
                        totalSpent += price;
                        spentPerRoommate.put(shopperName, spentPerRoommate.getOrDefault(shopperName, 0.0) + price);
                    }
                }

                int numRoommates = spentPerRoommate.size();
                double average = numRoommates > 0 ? totalSpent / numRoommates : 0.0;

                totalSpentTextView.setText(String.format("Total Spent: $%.2f", totalSpent));
                averageSpentTextView.setText(String.format("Average per Roommate: $%.2f", average));

                roommateSummaries.clear();
                for (Map.Entry<String, Double> entry : spentPerRoommate.entrySet()) {
                    double spent = entry.getValue();
                    roommateSummaries.add(new RoommateSummary(entry.getKey(), spent, spent - average));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load summary data", error.toException());
            }
        });
    }

    private void showSettleConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Settle Purchases")
                .setMessage("Are you sure you want to settle? This will clear all past purchases.")
                .setPositiveButton("Yes", (dialog, which) -> settlePurchases())
                .setNegativeButton("No", null)
                .show();
    }

    private void settlePurchases() {
        purchasedReference.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Purchases settled and cleared", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to clear purchases", e));
    }

    // Inner classes for RecyclerView
    private static class RoommateSummary {
        String name;
        double spent;
        double difference;

        RoommateSummary(String name, double spent, double difference) {
            this.name = name;
            this.spent = spent;
            this.difference = difference;
        }
    }

    private static class RoommateSummaryAdapter extends RecyclerView.Adapter<RoommateSummaryAdapter.ViewHolder> {
        private List<RoommateSummary> summaries;

        RoommateSummaryAdapter(List<RoommateSummary> summaries) {
            this.summaries = summaries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.roommate_summary_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RoommateSummary summary = summaries.get(position);
            holder.name.setText(summary.name);
            holder.spent.setText(String.format("Spent: $%.2f", summary.spent));
            holder.difference.setText(String.format("Diff: $%.2f", summary.difference));
            
            if (summary.difference >= 0) {
                holder.difference.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.difference.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public int getItemCount() {
            return summaries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, spent, difference;

            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.roommateNameTextView);
                spent = itemView.findViewById(R.id.roommateSpentTextView);
                difference = itemView.findViewById(R.id.roommateDifferenceTextView);
            }
        }
    }
}