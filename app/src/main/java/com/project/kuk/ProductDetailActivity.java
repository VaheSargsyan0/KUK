package com.project.kuk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productDescription, productPrice, productQuantity;
    private Button decreaseQuantity, increaseQuantity, orderButton;
    private ImageButton settingsButton;
    private int quantity = 1;
    private String productId, shopId, ownerId, userRole;
    private String imageUrl;

    private DatabaseReference productRef, cartRef, shopRef, userRef;
    private double priceValue = 0.0;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);
        productPrice = findViewById(R.id.productPrice);
        productQuantity = findViewById(R.id.productQuantity);
        decreaseQuantity = findViewById(R.id.decreaseQuantity);
        increaseQuantity = findViewById(R.id.increaseQuantity);
        orderButton = findViewById(R.id.orderButton);
        settingsButton = findViewById(R.id.settingsButton);

        productId = getIntent().getStringExtra("productId");
        shopId = getIntent().getStringExtra("shopId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (productId == null || shopId == null) {
            Toast.makeText(this, "Ошибка: отсутствует productId или shopId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productRef = FirebaseDatabase.getInstance().getReference("Shops").child(shopId).child("Products").child(productId);
        cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(currentUserId);
        shopRef = FirebaseDatabase.getInstance().getReference("Shops").child(shopId);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        loadProductDetails();
        checkPermissions();
        setupQuantityControls();
        setupOrderButton();
        setupSettingsButton();
    }

    private void loadProductDetails() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    productName.setText(snapshot.child("productName").getValue(String.class));
                    productDescription.setText(snapshot.child("description").getValue(String.class));
                    priceValue = snapshot.child("price").getValue(Double.class);
                    imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    productPrice.setText(String.format("%,.2f AMD", priceValue));
                    Glide.with(ProductDetailActivity.this).load(imageUrl).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Ошибка загрузки данных!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupQuantityControls() {
        decreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                productQuantity.setText(String.valueOf(quantity));
            }
        });

        increaseQuantity.setOnClickListener(v -> {
            quantity++;
            productQuantity.setText(String.valueOf(quantity));
        });
    }

    private void setupOrderButton() {
        orderButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                Toast.makeText(ProductDetailActivity.this, "Ошибка: пользователь не найден!", Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem cartItem = new CartItem(productId, productName.getText().toString(), priceValue, quantity, imageUrl, userId);

            cartRef.push().setValue(cartItem).addOnSuccessListener(aVoid -> {
                Toast.makeText(ProductDetailActivity.this, "Добавлено в корзину!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ProductDetailActivity.this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void checkPermissions() {
        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ownerId = snapshot.child("ownerId").getValue(String.class);
                    updateSettingsButtonVisibility();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("role").getValue(String.class);
                    updateSettingsButtonVisibility();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateSettingsButtonVisibility() {
        if (currentUserId.equals(ownerId) || "admin".equals(userRole)) {
            settingsButton.setVisibility(View.VISIBLE);
        } else {
            settingsButton.setVisibility(View.GONE);
        }
    }

    private void setupSettingsButton() {
        settingsButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(ProductDetailActivity.this, settingsButton);
            if (currentUserId.equals(ownerId) || "admin".equals(userRole)) {
                popupMenu.getMenu().add("Удалить");
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Удалить")) {
                    productRef.removeValue().addOnSuccessListener(aVoid -> finish());
                }
                return true;
            });
            popupMenu.show();
        });
    }
}




