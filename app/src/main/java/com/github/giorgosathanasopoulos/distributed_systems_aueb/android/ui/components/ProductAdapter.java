package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.R;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.Backend;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.Result;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Product;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils.DialogUtils;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, quantity, price, id;
        Button buyButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.typeLabel);
            id = itemView.findViewById(R.id.idLabel);
            name = itemView.findViewById(R.id.nameLabel);
            quantity = itemView.findViewById(R.id.quantityLabel);
            price = itemView.findViewById(R.id.priceLabel);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.type.setText("Type: " + p.getType());
        holder.id.setText("ID: " + p.getId());
        holder.name.setText(p.getName());
        holder.quantity.setText("Quantity: " + p.getQuantity());
        holder.price.setText("Price: $" + p.getPrice());

        holder.buyButton.setOnClickListener(v -> {
            if (p.decreaseQuantity(1)) {
                if (!Backend.sendBuyRequest(p.getStoreName(), p.getName(), 1, (Result<String, String> result) -> {
                    if (!result.isOk()) {
                        DialogUtils.showAlertDialog(context, "Error", "Failed to buy product: " + result.getError());
                        p.increaseQuantity(1);
                        notifyItemChanged(position);
                    }
                })) {
                    DialogUtils.showAlertDialog(context, "Error", "An error occurred while performing product purchase", "Ok", () -> {
                        p.increaseQuantity(1);
                        notifyItemChanged(position);
                    });
                    return;
                }

                notifyItemChanged(position);
                Toast.makeText(context, "Bought 1 " + p.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Out of stock!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}