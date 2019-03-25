package android.com.inventoryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.com.inventoryapp.data.ContractClass.InventoryEntry;
import android.com.inventoryapp.data.DbHelperClass;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int MIN_QUANTITY_VALUE = 0;
    private final Integer MAX_QUANTITY_VALUE = 2147483647;
    private String supplierContactNumber;
    private static final int EXISTING_ITEM_LOADER = 1;
    private Uri currentItemUri;
    private boolean itemHasChanged = false;
    private EditText productNameEditText;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierContactNumberEditText;
    private DbHelperClass dbHelperClass;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_editor);
        Intent intent = getIntent();
        currentItemUri = intent.getData();
        if (currentItemUri == null) {
            setTitle(getString(R.string.add_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        productNameEditText = findViewById(R.id.edit_product_name);
        productPriceEditText = findViewById(R.id.edit_product_price);
        productQuantityEditText = findViewById(R.id.edit_product_quantity);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        supplierContactNumberEditText = findViewById(R.id.edit_supplier_phone);
        Button quantityButtonMinus = findViewById(R.id.subtract_quantity);
        Button quantityButtonPlus = findViewById(R.id.add_quantity);


        quantityButtonPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String currentQuantityString = productQuantityEditText.getText().toString();
                int currentQuantityInt;
                if (currentQuantityString.length() == 0) {
                    currentQuantityInt = 1;
                    productQuantityEditText.setText(String.valueOf(currentQuantityInt));
                } else {
                    currentQuantityInt = Integer.parseInt(currentQuantityString) + 1;
                    if (currentQuantityInt <= MAX_QUANTITY_VALUE) {
                        productQuantityEditText.setText(String.valueOf(currentQuantityInt));
                    }
                }
            }
        });


        quantityButtonMinus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String currentQuantityString = productQuantityEditText.getText().toString();
                int currentQuantityInt;
                if (currentQuantityString.length() == 0) {
                    currentQuantityInt = 0;
                    productQuantityEditText.setText(String.valueOf(currentQuantityInt));
                } else {
                    currentQuantityInt = Integer.parseInt(currentQuantityString) - 1;
                    if (currentQuantityInt >= MIN_QUANTITY_VALUE) {
                        productQuantityEditText.setText(String.valueOf(currentQuantityInt));
                    }
                }
            }
        });

        dbHelperClass = new DbHelperClass(this);
        productNameEditText.setOnTouchListener(mTouchListener);
        productPriceEditText.setOnTouchListener(mTouchListener);
        productQuantityEditText.setOnTouchListener(mTouchListener);
        quantityButtonMinus.setOnTouchListener(mTouchListener);
        quantityButtonMinus.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierContactNumberEditText.setOnTouchListener(mTouchListener);
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveItem() {
        String productNameString = productNameEditText.getText().toString().trim();
        String productPriceString = productPriceEditText.getText().toString().trim();
        String productQuantityString = productQuantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierContactNumberString = supplierContactNumberEditText.getText().toString().trim();
        if (TextUtils.isEmpty(productNameString)) {
            productNameEditText.setError(getString(R.string.required_field));
            return;
        }
        if (TextUtils.isEmpty(productPriceString)) {
            productPriceEditText.setError(getString(R.string.required_field));
            return;
        }
        if (TextUtils.isEmpty(productQuantityString)) {
            productQuantityEditText.setError(getString(R.string.required_field));
            return;
        }
        if (TextUtils.isEmpty(supplierNameString)) {
            supplierNameEditText.setError(getString(R.string.required_field));
            return;
        }
        if (TextUtils.isEmpty(supplierContactNumberString)) {
            supplierContactNumberEditText.setError(getString(R.string.required_field));
            return;
        }
        int productPriceInt = Integer.parseInt(productPriceString);
        int productQuantityInt = Integer.parseInt(productQuantityString);


        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRICE, productPriceInt);
        values.put(InventoryEntry.COLUMN_QUANTITY, productQuantityInt);
        values.put(InventoryEntry.COLUMN_NAME_SUPPLIER, supplierNameString);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER_SUPPLIER, supplierContactNumberString);
        if (currentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.item_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowAffected = getContentResolver().update(currentItemUri, values, null, null);
            if (rowAffected == 0) {
                Toast.makeText(this, getString(R.string.item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void deleteItem() {
        if (currentItemUri != null) {
            int rowsDeleted;
            rowsDeleted = getContentResolver().delete(
                    currentItemUri,
                    null,
                    null
            );
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.error_deleting_item),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.item_deleted),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_quit));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_item));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void callSupplier() {
        Intent supplierNumberIntent = new Intent(Intent.ACTION_DIAL);
        supplierNumberIntent.setData(Uri.parse("tel:" + supplierContactNumber));
        startActivity(supplierNumberIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemUri == null) {
            MenuItem menuItem;
            menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.call_supplier);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveItem();
                return true;
            case R.id.call_supplier:
                callSupplier();
                break;
            case R.id.delete:
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(InventoryActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(InventoryActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_NAME_SUPPLIER,
                InventoryEntry.COLUMN_PHONE_NUMBER_SUPPLIER,
        };
        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEditText.setText("");
        productPriceEditText.setText("");
        productQuantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierContactNumberEditText.setText("");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME_SUPPLIER);
            int supplierContactNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE_NUMBER_SUPPLIER);
            String productName = cursor.getString(productNameColumnIndex);
            int productPrice = cursor.getInt(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            supplierContactNumber = cursor.getString(supplierContactNumberColumnIndex);
            productNameEditText.setText(productName);
            productPriceEditText.setText(String.valueOf(productPrice));
            productQuantityEditText.setText(String.valueOf(productQuantity));
            supplierNameEditText.setText(String.valueOf(supplierName));
            supplierContactNumberEditText.setText(String.valueOf(supplierContactNumber));
        }
    }
}