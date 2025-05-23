package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.R;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.Backend;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Filters;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response.FilterStoresResponse;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils.DialogUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils.DoubleUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.utils.StringUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FilterActivity extends AppCompatActivity {
    private EditText latitudeInput, longitudeInput, radiusInput;
    private ChipGroup foodTypeChips, starChips, priceChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle("Filter");

        latitudeInput = findViewById(R.id.latitudeInput);
        longitudeInput = findViewById(R.id.longitudeInput);
        radiusInput = findViewById(R.id.radiusInput);
        foodTypeChips = findViewById(R.id.foodTypeChips);
        starChips = findViewById(R.id.starChips);
        priceChips = findViewById(R.id.priceChips);

        Backend.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Backend.destroy();
    }

    public void onFilterButtonClicked(View view) {
        Optional<Double> latitudeOptional = DoubleUtils.tryParseDouble(latitudeInput.getText().toString());
        double latitude = 0;
        if (latitudeOptional.isPresent())
            latitude = latitudeOptional.get();

        Optional<Double> longitudeOptional = DoubleUtils.tryParseDouble(longitudeInput.getText().toString());
        double longitude = 0;
        if (longitudeOptional.isPresent())
            longitude = longitudeOptional.get();

        Optional<Double> radiusOptional = DoubleUtils.tryParseDouble(radiusInput.getText().toString());
        double radius = 6400; // larger estimate of earth's radius
        if (radiusOptional.isPresent())
            radius = radiusOptional.get();

        List<String> foodTypes = getStrings();

        List<Integer> stars = getIntegers();

        List<Integer> prices = getIntegers(stars);

        Filters filters = new Filters(latitude, longitude, radius, foodTypes, stars, prices);

        if (!Backend.sendFilterStoresRequest(filters, result -> {

            if (!result.isOk())
                DialogUtils.showAlertDialog(this, "Error", "Failed to send request: " + result.getError());

            else {
//                DialogUtils.showAlertDialog(this, "Info", "Received successful response from server");
                Intent intent = new Intent(this, ListBuyActivity.class);
                FilterStoresResponse response = JsonUtils.fromJson(result.getValue(), FilterStoresResponse.class).get(); // NOTE: we dont check optional cause we check in backend
                intent.putParcelableArrayListExtra("stores", response.getStores());
                startActivity(intent);
            }
        }))

            DialogUtils.showAlertDialog(this, "Error", "Failed to send request");
    }

    @NonNull
    private List<String> getStrings() {
        List<Integer> foodTypesInt = foodTypeChips.getCheckedChipIds();
        List<String> foodTypes = new ArrayList<>();
        foodTypes.add("pizzeria");
        if (!foodTypesInt.isEmpty()) {
            foodTypes.clear();
            foodTypesInt.forEach((Integer idx) -> {
                Chip chip = findViewById(idx);
                assert chip != null;
                assert chip.getText() != null;
                String foodType = chip.getText().toString();
                foodType = foodType.strip().trim().toLowerCase();
                foodTypes.add(foodType);
            });
        }
        return foodTypes;
    }

    @NonNull
    private List<Integer> getIntegers() {
        List<Integer> starsInt = starChips.getCheckedChipIds();
        List<Integer> stars = new ArrayList<>();
        stars.add(1);
        stars.add(2);
        stars.add(3);
        stars.add(4);
        stars.add(5);
        if (!starsInt.isEmpty()) {
            stars.clear();
            starsInt.forEach((Integer idx) -> {
                Chip chip = findViewById(idx);
                assert chip != null;
                assert chip.getText() != null;
                String text = chip.getText().toString();
                text = text.strip().trim().toLowerCase();
                assert text.contains("*");
                stars.add(StringUtils.countCharInStr(text, '*'));
            });
        }
        return stars;
    }

    @NonNull
    private List<Integer> getIntegers(List<Integer> stars) {
        List<Integer> pricesInt = priceChips.getCheckedChipIds();
        List<Integer> prices = new ArrayList<>();
        prices.add(1);
        prices.add(2);
        prices.add(3);
        if (!pricesInt.isEmpty()) {
            prices.clear();
            pricesInt.forEach((Integer idx) -> {
                Chip chip = findViewById(idx);
                assert chip != null;
                assert chip.getText() != null;
                String text = chip.getText().toString();
                text = text.strip().trim().toLowerCase();
                assert text.contains("$");
                stars.add(StringUtils.countCharInStr(text, '$'));
            });
        }
        return prices;
    }
}