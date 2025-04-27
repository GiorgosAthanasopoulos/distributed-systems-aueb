package giorgosathanasopoulos.com.github.distributed_systems_aueb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Store {

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
    private final List<Product> c_Products = new ArrayList<>();

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

    public String getStoreName() {
        return c_StoreName;
    }

    public double getLatitude() {
        return c_Latitude;
    }

    public double getLongitutde() {
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

    public List<Product> getProducts() {
        return c_Products;
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
                .filter((Product p) -> {
                    return p.getName().equals(p_Name);
                })
                .findFirst();

        if (product.isEmpty()) {
            return false;
        }

        product.get().setHidden(true);
        return true;
    }

    private void calculateInflationIndex() {
        int sum = 0;

        for (Product product : c_Products) {
            sum += product.getPrice();
        }

        int avg = sum / c_Products.size();
        m_InflationIndex = avg > 15 ? 3 : avg > 5 ? 2 : 1;
    }

    public boolean containsProduct(Product p_Product) {
        return c_Products.stream().anyMatch(p -> p.getName().equals(p_Product.getName()));
    }

    public boolean containsProduct(String productName) {
        return c_Products.stream().anyMatch(p -> p.getName().equals(productName));
    }

    public int getInflationIndex() {
        return m_InflationIndex;
    }

    public Optional<Product> getProduct(String productName) {
        return c_Products.stream().filter(p -> p.getName().equals(productName)).findFirst();
    }
}
