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

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.project.kuk.R;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private EditText productNameEt, descriptionEt, priceEt;
    private ImageView productImageView;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseDatabase database;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productNameEt = findViewById(R.id.productNameEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        priceEt = findViewById(R.id.priceEt);
        productImageView = findViewById(R.id.productImageView);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button addProductButton = findViewById(R.id.addProductButton);

        database = FirebaseDatabase.getInstance();
        shopId = getIntent().getStringExtra("shopId");

        selectImageButton.setOnClickListener(v -> openImagePicker());
        addProductButton.setOnClickListener(v -> addProduct());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            productImageView.setImageURI(imageUri);
        }
    }

    private void addProduct() {
        String productName = productNameEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        String priceStr = priceEt.getText().toString().trim();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr)) {
            showToast("Все поля должны быть заполнены!");
            return;
        }

        double price = Double.parseDouble(priceStr);
        String productId = database.getReference("Shops").child(shopId).child("Products").push().getKey();

        if (imageUri != null) {
            FirebaseStorage.getInstance().getReference("product_images").child(productId + ".jpg")
                    .putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> saveProductData(productId, productName, description, uri.toString(), price))
                            .addOnFailureListener(e -> showToast("Ошибка загрузки изображения: " + e.getMessage())));
        } else {
            saveProductData(productId, productName, description, "", price);
        }
    }

    private void saveProductData(String productId, String productName, String description, String imageUrl, double price) {
        HashMap<String, Object> productData = new HashMap<>();
        productData.put("productId", productId);
        productData.put("productName", productName);
        productData.put("description", description);
        productData.put("imageUrl", imageUrl);
        productData.put("price", price);

        database.getReference("Shops").child(shopId).child("Products").child(productId).setValue(productData)
                .addOnSuccessListener(unused -> {
                    showToast("Товар добавлен!");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Ошибка добавления товара: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
