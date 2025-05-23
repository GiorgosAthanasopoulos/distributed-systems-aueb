package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.R;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;

import java.util.ArrayList;
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

        Objects.requireNonNull(getSupportActionBar()).setTitle("List/Buy");

        ArrayList<Store> stores = getIntent().getParcelableArrayListExtra("stores");
        if (stores == null);
            // TODO: smth went wrong -> go back to filter page
        if (stores.isEmpty());
            // TODO: no search results found, try again with new filters -> go back to filter page

        // TODO: render products (create component/fragment for each?)
    }
}
