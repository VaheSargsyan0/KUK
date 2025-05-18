package com.project.kuk;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shopList;

    public ShopAdapter(List<Shop> shopList) {
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.shopNameTextView.setText(shop.getShopName());
        holder.descriptionTextView.setText(shop.getDescription());

        if (shop.getLogoUrl() != null && !shop.getLogoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(shop.getLogoUrl()).into(holder.shopLogoImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ShopDetailActivity.class);
            intent.putExtra("shopId", shop.getShopId());
            intent.putExtra("shopName", shop.getShopName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopNameTextView, descriptionTextView;
        ImageView shopLogoImageView;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            descriptionTextView = itemView.findViewById(R.id.shopDescriptionTextView);
            shopLogoImageView = itemView.findViewById(R.id.shopLogoImageView);
        }
    }
}


