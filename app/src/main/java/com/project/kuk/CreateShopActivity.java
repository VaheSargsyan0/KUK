package com.project.kuk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

public class CreateShopActivity extends AppCompatActivity {

    private EditText shopNameEt, descriptionEt;
    private ImageView logoImageView;
    private Uri logoUri;
    private static final int PICK_LOGO_REQUEST = 1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);

        shopNameEt = findViewById(R.id.shopNameEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        logoImageView = findViewById(R.id.logoImageView);
        Button selectLogoButton = findViewById(R.id.selectLogoButton);
        Button createShopButton = findViewById(R.id.createShopButton);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        selectLogoButton.setOnClickListener(v -> openLogoPicker());
        createShopButton.setOnClickListener(v -> createShop());
    }

    private void openLogoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_LOGO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_LOGO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            logoUri = data.getData();
            logoImageView.setImageURI(logoUri);
        }
    }

    private void createShop() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String shopName = shopNameEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(shopName) || TextUtils.isEmpty(description)) {
            showToast("Название и описание должны быть заполнены!");
            return;
        }

        String shopId = database.getReference("Shops").push().getKey();

        if (logoUri != null) {
            FirebaseStorage.getInstance().getReference("shop_logos").child(shopId + ".jpg")
                    .putFile(logoUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> saveShopData(shopId, shopName, description, uri.toString(), user.getUid()))
                            .addOnFailureListener(e -> showToast("Ошибка загрузки логотипа: " + e.getMessage())));
        } else {
            saveShopData(shopId, shopName, description, "", user.getUid());
        }
    }

    private void saveShopData(String shopId, String shopName, String description, String logoUrl, String ownerId) {
        HashMap<String, Object> shopData = new HashMap<>();
        shopData.put("shopId", shopId);
        shopData.put("shopName", shopName);
        shopData.put("description", description);
        shopData.put("logoUrl", logoUrl);
        shopData.put("ownerId", ownerId);

        database.getReference("Shops").child(shopId).setValue(shopData)
                .addOnSuccessListener(unused -> {
                    showToast("Магазин создан!");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Ошибка создания магазина: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
