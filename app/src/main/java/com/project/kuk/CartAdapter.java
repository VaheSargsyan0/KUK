package com.project.kuk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemRemovedListener removedListener;

    public CartAdapter(List<CartItem> cartItems, OnCartItemRemovedListener removedListener) {
        this.cartItems = cartItems;
        this.removedListener = removedListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.productName.setText(item.getProductName());
        holder.productQuantity.setText(String.valueOf(item.getQuantity()));
        holder.productPrice.setText(String.format("%,.2f AMD", item.getTotalPrice()));

        Glide.with(holder.itemView.getContext()).load(item.getImageUrl()).into(holder.productImage);

        holder.removeButton.setOnClickListener(v -> removedListener.onCartItemRemoved(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;
        Button removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productNameCart);
            productPrice = itemView.findViewById(R.id.productPriceCart);
            productQuantity = itemView.findViewById(R.id.productQuantityCart);
            productImage = itemView.findViewById(R.id.productImageCart);
            removeButton = itemView.findViewById(R.id.removeFromCartButton);
        }
    }

    public interface OnCartItemRemovedListener {
        void onCartItemRemoved(CartItem item);
    }
}


