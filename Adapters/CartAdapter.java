package com.example.apponline.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.apponline.R;
import com.example.apponline.models.OrderItem;
import com.example.apponline.firebase.CartManager;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<OrderItem> cartList;
    private final CartUpdateListener listener;

    public interface CartUpdateListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(Context context, List<OrderItem> cartList, CartUpdateListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }
    public void updateItems(List<OrderItem> newCartList) {
        this.cartList = newCartList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        OrderItem item = cartList.get(position); // Dùng position vì nó ổn định hơn khi binding

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("Giá: %,.0f VNĐ", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        String size = item.getselectedSize();
        if (size != null && !size.isEmpty()) {
            holder.tvSize.setText("Size: " + size);
            holder.tvSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvSize.setVisibility(View.GONE);
        }
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.product_placeholder);
        }

        holder.btnMinus.setOnClickListener(v -> {
            int clickPosition = holder.getBindingAdapterPosition();
            if (clickPosition == RecyclerView.NO_POSITION) return;

            OrderItem clickedItem = cartList.get(clickPosition);
            int currentQty = clickedItem.getQuantity();

            if (currentQty <= 1) {

                CartManager.getInstance().removeItem(clickedItem);
                cartList.remove(clickPosition);

                notifyItemRemoved(clickPosition);


                listener.onItemRemoved();
            } else {
                int newQty = currentQty - 1;
                CartManager.getInstance().updateQuantity(clickedItem, newQty);
                notifyItemChanged(clickPosition);
                listener.onQuantityChanged();
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            int clickPosition = holder.getBindingAdapterPosition();
            if (clickPosition == RecyclerView.NO_POSITION) return;

            OrderItem clickedItem = cartList.get(clickPosition);
            int newQty = clickedItem.getQuantity() + 1;

            CartManager.getInstance().updateQuantity(clickedItem, newQty);
            notifyItemChanged(clickPosition);
            listener.onQuantityChanged();
        });

        holder.btnRemove.setOnClickListener(v -> {
            int clickPosition = holder.getBindingAdapterPosition();
            if (clickPosition == RecyclerView.NO_POSITION) return;

            OrderItem clickedItem = cartList.get(clickPosition);

            CartManager.getInstance().removeItem(clickedItem);
            cartList.remove(clickPosition);

            notifyItemRemoved(clickPosition);

            listener.onItemRemoved();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvSize;
        ImageButton btnMinus, btnPlus, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.iv_cart_product);
            tvName = itemView.findViewById(R.id.tv_cart_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            tvSize = itemView.findViewById(R.id.tv_cart_size);

            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}