package giorgosathanasopoulos.com.github.distributed_systems_aueb.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Store {

    @SerializedName("StoreName")
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

    @SerializedName("StoreLogo")
    private final String c_StoreLogo;

    @SerializedName("Products")
    private final List<Product> c_Products = new ArrayList<>();

    @SerializedName("InflationIndex")
    private int m_InflationIndex;

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

    public void addProduct(Product p_Product) {
        c_Products
                .stream()
                .filter((Product product) -> product.getProductName().equals(p_Product.getProductName()))
                .findFirst()
                .ifPresentOrElse(
                        (Product product) -> {
                        },
                        () -> {
                            c_Products.add(p_Product);
                        });
    }

    public void removeProduct(String p_Name) {
        c_Products
                .stream()
                .filter((Product product) -> {
                    return product.getProductName().equals(p_Name);
                })
                .findFirst()
                .ifPresent((Product product) -> {
                    product.hide();
                });
    }

    private void calculateInflationIndex() {
        int sum = 0;

        for (Product product : c_Products) {
            sum += product.getPrice();
        }

        int avg = sum / c_Products.size();
        m_InflationIndex = avg > 15 ? 3 : avg > 5 ? 2 : 1;
    }
}
