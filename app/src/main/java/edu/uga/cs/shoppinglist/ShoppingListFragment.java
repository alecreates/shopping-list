package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

/**
 * Fragment that displays the active shopping list for the roommate shopping app.
 *
 * <p>This fragment allows users to:
 * <ul>
 *     <li>View shopping items stored in Firebase Realtime Database</li>
 *     <li>Add new items</li>
 *     <li>Edit existing items</li>
 *     <li>Delete items with confirmation</li>
 *     <li>Mark items as purchased, moving them to a separate database node</li>
 * </ul>
 *
 * The UI is backed by a {@link RecyclerView} using {@link ShoppingListAdapter}.
 * </p>
 */

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

    /**
     * Inflates the fragment layout, initializes the RecyclerView, adapter,
     * Firebase database references, and sets up UI event listeners.
     *
     * @param inflater LayoutInflater used to inflate the fragment view
     * @param container Parent view that the fragment UI attaches to
     * @param savedInstanceState Previously saved state (if any)
     * @return the root View for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAddItem = view.findViewById(R.id.fabAddItem);

        shoppingItemList = new ArrayList<>();
        adapter = new ShoppingListAdapter(
                shoppingItemList,
                new ShoppingListAdapter.OnItemActionListener() {

                    @Override
                    public void onPurchasedClick(ShoppingItem item) {
                        markItemAsPurchased(item);
                    }

                    @Override
                    public void onEditClick(ShoppingItem item) {
                        showEditItemDialog(item);
                    }

                    @Override
                    public void onDeleteClick(ShoppingItem item) {
                        deleteItem(item);
                    }
                },
                false
        );
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        shoppingReference = FirebaseDatabase.getInstance().getReference("shopping_items");
        purchasedReference = FirebaseDatabase.getInstance().getReference("purchased_items");

        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        loadShoppingItems();

        return view;
    }

    /**
     * Callback used by the item dialog to return validated user input.
     */
    public interface OnItemSaved {
        void onSave(String itemName);
    }

    /**
     * Displays a reusable dialog for adding or editing a shopping item.
     *
     * <p>This method is used by both the "Add Item" and "Edit Item" flows.
     * It handles input validation and delegates saving logic via a callback.</p>
     *
     * @param title Title displayed on the dialog
     * @param positiveText Text for the confirm button (e.g., "Add", "Save")
     * @param initialText Pre-filled text for editing (null for add mode)
     * @param callback Callback executed when a valid item name is submitted
     */
    private void showItemDialog(String title, String positiveText, String initialText, OnItemSaved callback) {

        EditText input = new EditText(getContext());
        input.setHint("Enter item name");

        if (initialText != null) {
            input.setText(initialText);
            input.setSelection(initialText.length());
        }

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton(positiveText, (dialog, which) -> {

                    String itemName = input.getText().toString().trim();

                    if (!itemName.isEmpty()) {
                        callback.onSave(itemName);
                    } else {
                        Toast.makeText(getContext(),
                                "Item name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Opens a dialog to add a new shopping item.
     *
     * <p>Uses {@link #showItemDialog(String, String, String, OnItemSaved)}
     * with no pre-filled text.</p>
     */
    private void showAddItemDialog() {
        showItemDialog(
                "Add Item",
                "Add",
                null,
                this::addItemToDatabase
        );
    }

    /**
     * Opens a dialog to edit an existing shopping item.
     *
     * <p>The current item name is pre-filled and updated in Firebase upon confirmation.</p>
     *
     * @param item The shopping item being edited
     */
    private void showEditItemDialog(ShoppingItem item) {
        showItemDialog(
                "Edit Item",
                "Save",
                item.getItemName(),
                newName -> updateItemInDatabase(item, newName)
        );
    }

    /**
     * Updates the name of an existing shopping item in Firebase.
     *
     * @param item The item being updated (must contain a valid Firebase key)
     * @param newName The new name to store in the database
     */
    private void updateItemInDatabase(ShoppingItem item, String newName) {

        if (item.getKey() == null) return;

        shoppingReference.child(item.getKey())
                .child("itemName")
                .setValue(newName)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Item updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update item", e)
                );
    }

    /**
     * Adds a new shopping item to Firebase under the "shopping_items" node.
     *
     * @param itemName Name of the item to add
     */
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

    /**
     * Initiates deletion of a shopping item by showing a confirmation dialog.
     *
     * @param item The item to be deleted
     */
    private void deleteItem(ShoppingItem item) {
        showDeleteConfirmationDialog(item);
    }


    /**
     * Displays a confirmation dialog before deleting an item.
     *
     * <p>Prevents accidental deletion by requiring user confirmation.</p>
     *
     * @param item The item pending deletion
     */
    private void showDeleteConfirmationDialog(ShoppingItem item) {

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getItemName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteItemFromDatabase(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes a shopping item from Firebase Realtime Database.
     *
     * @param item The item to remove (must contain a valid Firebase key)
     */
    private void deleteItemFromDatabase(ShoppingItem item) {

        if (item.getKey() == null) return;

        shoppingReference.child(item.getKey())
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete item", e)
                );
    }

    /**
     * Moves an item from the shopping list to the purchased list in Firebase.
     *
     * <p>This method:
     * <ol>
     *     <li>Creates a copy of the item in the "purchased_items" node</li>
     *     <li>Removes the original item from "shopping_items"</li>
     * </ol>
     * </p>
     *
     * @param item The item being marked as purchased
     */
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

    /**
     * Listens to Firebase "shopping_items" node and loads all items into the RecyclerView.
     *
     * <p>This uses a realtime ValueEventListener so updates automatically reflect in the UI.</p>
     */
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