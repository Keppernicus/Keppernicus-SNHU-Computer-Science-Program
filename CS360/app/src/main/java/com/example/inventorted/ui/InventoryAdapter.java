package com.example.inventorted.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorted.R;
import com.example.inventorted.data.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * InventoryAdapter - Binds inventory item data to the RecyclerView grid.
 * Each row displays the item name, quantity, and a delete button.
 * Tapping a row opens an edit dialog (handled via the listener interface).
 * Tapping the delete button removes the item (also via the listener).
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;

    /**
     * Callback interface so the Activity handles the actual database operations.
     * The adapter just displays data and reports user actions back up.
     */
    public interface OnItemActionListener {
        void onDeleteItem(InventoryItem item, int position);
        void onEditItem(InventoryItem item, int position);
    }

    public InventoryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row layout built in Project Two
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);

        holder.textViewItemName.setText(item.getName());
        holder.textViewItemQuantity.setText(String.valueOf(item.getQuantity()));

        // delete button removes this item from the database
        holder.buttonDeleteItem.setOnClickListener(v ->
                listener.onDeleteItem(item, holder.getAdapterPosition()));

        // tapping the row itself lets the user edit the quantity
        holder.itemView.setOnClickListener(v ->
                listener.onEditItem(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * replaces the current dataset and refreshes the grid.
     * called after any CRUD operation changes the data.
     */
    public void updateItems(List<InventoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    /**
     * viewHolder caches references to the views in each row
     * so we're not calling findViewById on every scroll.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewItemQuantity;
        ImageButton buttonDeleteItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewItemQuantity = itemView.findViewById(R.id.textViewItemQuantity);
            buttonDeleteItem = itemView.findViewById(R.id.buttonDeleteItem);
        }
    }
}