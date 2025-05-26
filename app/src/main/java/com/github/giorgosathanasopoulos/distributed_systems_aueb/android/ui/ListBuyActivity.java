package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.R;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Product;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.ui.components.ProductFragment;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ListBuyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_buy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle("List/Buy Items");

        ArrayList<Store> stores = getIntent().getParcelableArrayListExtra("stores");
        if (stores == null) {
            DialogUtils.showAlertDialog(this, "Error", "Something went wrong when fetchin results", "Ok", () -> {
                startActivity(new Intent(this, FilterActivity.class));
            });
            return;
        }
        if (stores.isEmpty()) {
            DialogUtils.showAlertDialog(this, "Error", "No results found! Try again with different filters!", "Ok", () -> {
                startActivity(new Intent(this, FilterActivity.class));
            });
            return;
        }

        ArrayList<Product> productList = new ArrayList<>();
        for (Store store : stores) {
            productList.addAll(store.getProducts(false));
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("product_list", productList); // Note key: "product_list"

        ProductFragment fragment = new ProductFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.productFragmentContainer, fragment)
                .commit();
    }
}
