package giorgosathanasopoulos.com.github.distributed_systems_aueb.model;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.uid.UID;

public class Product {

    @SerializedName("StoreName")
    private final String c_StoreName;

    @SerializedName("Name")
    private final String c_Name;

    @SerializedName("Type")
    private final String c_Type;

    @SerializedName("Quantity")
    private int m_Quantity;

    @SerializedName("Price")
    private final double c_Price;

    @SerializedName("Visible")
    private boolean m_Visible = true; // NOTE: remember to hide only from client!

    @SerializedName("Id")
    private final int c_Id;

    public Product(
            String p_StoreName,
            String p_Name,
            String p_Type,
            int p_Quantity,
            double p_Price) {
        this.c_StoreName = p_StoreName;
        this.c_Name = p_Name;
        this.c_Type = p_Type;
        this.m_Quantity = p_Quantity;
        this.c_Price = p_Price;
        this.m_Visible = true;
        c_Id = UID.getNextUID();
    }

    public String getStoreName() {
        return c_StoreName;
    }

    public String getName() {
        return c_Name;
    }

    public String getType() {
        return c_Type;
    }

    public int getQuantity() {
        return m_Quantity;
    }

    public double getPrice() {
        return c_Price;
    }

    public int getId() {
        return c_Id;
    }

    public void setQuantity(int p_Quantity) {
        this.m_Quantity = p_Quantity;
    }

    public boolean isVisible() {
        return m_Visible;
    }

    public void setHidden(boolean hidden) {
        m_Visible = !hidden;
    }

    public void toggleHidden() {
        m_Visible = !m_Visible;
    }

    public boolean decreaseQuantity(int p_Amount) {
        if (p_Amount <= 0 || p_Amount > m_Quantity) {
            return false;
        }

        m_Quantity -= p_Amount;
        return true;
    }

    public boolean increaseQuantity(int p_Amount) {
        if (p_Amount <= 0) {
            return false;
        }

        m_Quantity += p_Amount;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("\t\"StoreName\": \"" + c_StoreName + "\"\n");
        sb.append("\t\"Name\": \"" + c_Name + "\"\n");
        sb.append("\t\"Type\": \"" + c_Type + "\"\n");
        sb.append("\t\"Quantity\": " + m_Quantity + "\n");
        sb.append("\t\"Price\": " + c_Price + "\n");
        sb.append("\t\"Visible\": " + m_Visible + "\n");
        sb.append("},\n");

        return sb.toString();
    }

    public boolean getVisible() {
        return m_Visible;
    }
}
