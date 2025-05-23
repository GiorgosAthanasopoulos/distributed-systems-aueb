package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.uid.UID;

public class Store implements Parcelable {

    @SerializedName("Name")
    private final String c_StoreName;

    @SerializedName("Latitude")
    private final double c_Latitude;

    @SerializedName("Longitude")
    private final double c_Longitude;

    @SerializedName("FoodCategory")
    private final String c_FoodCategory;

    @SerializedName("Stars")
    private int m_Stars;

    @SerializedName("NoOfVotes")
    private int m_NoOfVotes;

    @SerializedName("LogoPath")
    private final String c_StoreLogo;

    @SerializedName("Products")
    private List<Product> c_Products = new ArrayList<>();

    @SerializedName("InflationIndex")
    private int m_InflationIndex;

    @SerializedName("Id")
    private final int c_Id;

    public Store(
            String p_StoreName,
            double p_Latitude,
            double p_Longitude,
            String p_FoodCategory,
            int p_Stars,
            int p_NoOfVotes,
            String p_StoreLogo) {
        this.c_StoreName = p_StoreName;
        this.c_Latitude = p_Latitude;
        this.c_Longitude = p_Longitude;
        this.c_FoodCategory = p_FoodCategory;
        this.m_Stars = p_Stars;
        this.m_NoOfVotes = p_NoOfVotes;
        this.c_StoreLogo = p_StoreLogo;
        c_Id = UID.getNextUID();

        calculateInflationIndex();
    }

    protected Store(Parcel in) {
        c_StoreName = in.readString();
        c_Latitude = in.readDouble();
        c_Longitude = in.readDouble();
        c_FoodCategory = in.readString();
        m_Stars = in.readInt();
        m_NoOfVotes = in.readInt();
        c_StoreLogo = in.readString();
        c_Products = in.createTypedArrayList(Product.CREATOR);
        m_InflationIndex = in.readInt();
        c_Id = in.readInt();
    }

    public static final Creator<Store> CREATOR = new Creator<>() {
        @Override
        public Store createFromParcel(Parcel in) {
            return new Store(in);
        }

        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };

    public String getStoreName() {
        return c_StoreName;
    }

    public double getLatitude() {
        return c_Latitude;
    }

    public double getLongitude() {
        return c_Longitude;
    }

    public String getFoodCategory() {
        return c_FoodCategory;
    }

    public int getStars() {
        return m_Stars;
    }

    public int getNoOfVotes() {
        return m_NoOfVotes;
    }

    public String getStoreLogo() {
        return c_StoreLogo;
    }

    public List<Product> getProducts(boolean p_isAdmin) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return c_Products
                    .stream()
                    .filter(p -> p.isVisible() || p_isAdmin)
                    .toList();
        }
        return List.of();
    }

    public boolean addProduct(Product p_Product) {
        if (p_Product == null) {
            return false;
        }

        Optional<Product> product = c_Products
                .stream()
                .filter(p -> p.getName().equals(p_Product.getName()))
                .findFirst();
        if (product.isPresent()) {
            return false;
        }

        c_Products.add(p_Product);
        return true;
    }

    public boolean removeProduct(String p_Name) {
        Optional<Product> product = c_Products
                .stream()
                .filter((Product p) -> p.getName().equals(p_Name))
                .findFirst();

        if (product.isEmpty()) {
            return false;
        }

        product.get().setHidden(true);
        return true;
    }

    public void calculateInflationIndex() {
        int sum = 0;

        for (Product product : c_Products) {
            sum += (int) product.getPrice();
        }

        int avg = !c_Products.isEmpty() ? sum / c_Products.size() : 0;
        m_InflationIndex = avg > 15 ? 3 : avg > 5 ? 2 : 1;
    }

    public boolean containsProduct(Product p_Product) {
        return c_Products.stream().anyMatch(p -> p.getName().equals(p_Product.getName()));
    }

    public boolean containsProduct(String p_ProductName) {
        return c_Products.stream().anyMatch(p -> p.getName().equals(p_ProductName));
    }

    public int getInflationIndex() {
        return m_InflationIndex;
    }

    public Optional<Product> getProduct(String p_ProductName) {
        return c_Products.stream().filter(p -> p.getName().equals(p_ProductName)).findFirst();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(c_StoreName);
        dest.writeDouble(c_Latitude);
        dest.writeDouble(c_Longitude);
        dest.writeString(c_FoodCategory);
        dest.writeInt(m_Stars);
        dest.writeInt(m_NoOfVotes);
        dest.writeString(c_StoreLogo);
        dest.writeTypedList(c_Products);
        dest.writeInt(m_InflationIndex);
        dest.writeInt(c_Id);
    }
}
