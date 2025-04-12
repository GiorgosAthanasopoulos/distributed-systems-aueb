package giorgosathanasopoulos.com.github.distributed_systems_aueb.model;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("ProductName")
    private final String c_ProductName;

    @SerializedName("ProductType")
    private final String c_ProductType;

    @SerializedName("AvailableAmount")
    private int c_AvailableAmount;

    @SerializedName("Price")
    private final double c_Price;

    private transient boolean m_Visible = true;

    public Product(
            String productName,
            String productType,
            int availableAmount,
            double price) {
        this.c_ProductName = productName;
        this.c_ProductType = productType;
        this.c_AvailableAmount = availableAmount;
        this.c_Price = price;
    }

    public String getProductName() {
        return c_ProductName;
    }

    public String getProductType() {
        return c_ProductType;
    }

    public int getAvailableAmount() {
        return c_AvailableAmount;
    }

    public double getPrice() {
        return c_Price;
    }

    public void setAvailableAmount(int p_AvailableAmount) {
        this.c_AvailableAmount = p_AvailableAmount;
    }

    public boolean isVisible() {
        return m_Visible;
    }

    public void hide() {
        m_Visible = false;
    }
}
