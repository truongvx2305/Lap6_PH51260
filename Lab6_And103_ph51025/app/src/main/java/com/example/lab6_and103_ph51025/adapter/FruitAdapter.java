package com.example.lab6_and103_ph51025.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lab6_and103_ph51025.Activity.UpdateFruitActivity;
import com.example.lab6_and103_ph51025.R;
import com.example.lab6_and103_ph51025.databinding.ItemFruitBinding;
import com.example.lab6_and103_ph51025.model.Fruit;

import java.util.ArrayList;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Fruit> list;
    private FruitClick fruitClick;

    public FruitAdapter(Context context, ArrayList<Fruit> list, FruitClick fruitClick) {
        this.context = context;
        this.list = list;
        this.fruitClick = fruitClick;
    }

    public interface FruitClick {
        void delete(Fruit fruit);
        void edit(Fruit fruit);
        void showDetail(Fruit fruit);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFruitBinding binding = ItemFruitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fruit fruit = list.get(position);

        if (fruit == null) {
            Log.e("FruitAdapter", "Fruit object is null at position: " + position);
            return; // Bỏ qua nếu dữ liệu fruit null
        }

        // Set text fields
        holder.binding.tvName.setText(fruit.getName());
        holder.binding.tvPriceQuantity.setText("Price: " + fruit.getPrice() + " - Quantity: " + fruit.getQuantity());
        holder.binding.tvDes.setText(fruit.getDescription());

        // Handle image loading with Glide
        if (fruit.getImage() != null && !fruit.getImage().isEmpty()) {
            String url = fruit.getImage().get(0); // Lấy URL đầu tiên trong danh sách
            String newUrl = url.replace("localhost", "10.0.2.2"); // Replace localhost for emulator

            Glide.with(holder.itemView.getContext()) // Use itemView context for lifecycle management
                    .load(newUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // Hiển thị ảnh chờ
                    .error(R.drawable.baseline_broken_image_24) // Hiển thị ảnh lỗi
                    .into(holder.binding.img);
        } else {
            // Nếu không có ảnh, dùng ảnh mặc định
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.baseline_broken_image_24) // Ảnh mặc định khi không có URL
                    .into(holder.binding.img);
        }

        // Set up Edit button
        holder.binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateFruitActivity.class);
            intent.putExtra("fruit_name", fruit.getName());
            intent.putExtra("fruit_price", fruit.getPrice());
            intent.putExtra("fruit_quantity", fruit.getQuantity());
            intent.putExtra("fruit_description", fruit.getDescription());
            intent.putExtra("fruit_id", fruit.get_id()); // Truyền thêm ID của fruit
            context.startActivity(intent);
        });

        // Set up Delete button
        holder.binding.btnDelete.setOnClickListener(v -> {
            fruitClick.delete(fruit); // Gọi phương thức delete
        });

        // Log để kiểm tra URL
        Log.d("FruitAdapter", "onBindViewHolder: " + (fruit.getImage() == null || fruit.getImage().isEmpty()
                ? "No image" : fruit.getImage().get(0)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemFruitBinding binding;

        public ViewHolder(ItemFruitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
