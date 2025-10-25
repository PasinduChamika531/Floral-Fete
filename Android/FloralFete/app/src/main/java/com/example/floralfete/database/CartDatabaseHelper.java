package com.example.floralfete.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.floralfete.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cart.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CART = "cart";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PRODUCT_ID = "productId";
    private static final String COLUMN_PRODUCT_NAME = "productName";
    private static final String COLUMN_PRODUCT_IMAGE = "productImage";
    private static final String COLUMN_PRODUCT_PRICE = "productPrice";
    private static final String COLUMN_QUANTITY = "quantity";

    public CartDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_ID + " TEXT UNIQUE, " +
                COLUMN_PRODUCT_NAME + " TEXT, " +
                COLUMN_PRODUCT_IMAGE + " TEXT, " +
                COLUMN_PRODUCT_PRICE + " REAL, " +
                COLUMN_QUANTITY + " INTEGER)";
        db.execSQL(CREATE_CART_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    // Add or update item in cart
    public void addOrUpdateCartItem(CartItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_ID, item.getProductId());
        values.put(COLUMN_PRODUCT_NAME, item.getProductName());
        values.put(COLUMN_PRODUCT_IMAGE, item.getProductImage());
        values.put(COLUMN_PRODUCT_PRICE, item.getProductPrice());
        values.put(COLUMN_QUANTITY, item.getQuantity());


        db.insertWithOnConflict(TABLE_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Get all cart items
    public List<CartItem> getAllCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(TABLE_CART, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                );
                cartItems.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return cartItems;
    }

    // Update item quantity
    public void updateCartItemQuantity(String productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, quantity);

        // Use update method to change quantity for specific productId
        db.update(TABLE_CART, values, COLUMN_PRODUCT_ID + "=?", new String[]{productId});
        db.close();
    }

    // Remove item from cart
    public void removeCartItem(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_PRODUCT_ID + "=?", new String[]{productId});
        db.close();
    }

    // Clear all cart items
    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, null, null);
        db.close();
    }
}
