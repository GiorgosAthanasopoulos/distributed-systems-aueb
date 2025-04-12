package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class IncreaseQuantityRequest extends Request {

    @SerializedName("StoreName")
    private final String c_StoreName;

    @SerializedName("ProductName")
    private final String c_ProductName;

    @SerializedName("Quantity")
    private final int c_Quantity;

    public IncreaseQuantityRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action,
            String p_StoreName,
            String p_ProductName,
            int p_Quantity) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_StoreName = p_StoreName;
        this.c_ProductName = p_ProductName;
        this.c_Quantity = p_Quantity;
    }

    public String getStoreName() {
        return c_StoreName;
    }

    public String getProductName() {
        return c_ProductName;
    }

    public int getQuantity() {
        return c_Quantity;
    }
}
