package android.com.inventoryapp;

import android.com.inventoryapp.data.ContractClass.InventoryEntry;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

class InventoryAdapter extends CursorAdapter {
    InventoryAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView itemNameTextView = view.findViewById(R.id.item_name);
        TextView itemPriceTextView = view.findViewById(R.id.display_item_price);
        TextView itemQuantityTextView = view.findViewById(R.id.display_quantity);
        int itemNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int itemPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int itemQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        String itemName = cursor.getString(itemNameColumnIndex);
        int itemPrice = cursor.getInt(itemPriceColumnIndex);
        int itemQuantity = cursor.getInt(itemQuantityColumnIndex);
        itemNameTextView.setText(itemName);
        itemPriceTextView.setText(String.valueOf(itemPrice));
        itemQuantityTextView.setText(String.valueOf(itemQuantity));
        int itemIdColumIndex = cursor.getColumnIndex(InventoryEntry._ID);
        final long itemId = Integer.parseInt(cursor.getString(itemIdColumIndex));
        final int quantityValue = cursor.getInt(itemQuantityColumnIndex);
        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
                String updatedQuantity = String.valueOf(quantityValue - 1);
                if (Integer.parseInt(updatedQuantity) >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, updatedQuantity);
                    context.getContentResolver().update(currentUri, values, null, null);
                }
            }
        });
    }
}
