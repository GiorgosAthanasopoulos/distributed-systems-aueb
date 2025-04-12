package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class RemoveProductRequest extends Request {

    @SerializedName("StoreName")
    private final String c_StoreName;

    @SerializedName("ProductName")
    private final String c_ProductName;

    public RemoveProductRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action,
            String p_StoreName,
            String p_ProductName) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_StoreName = p_StoreName;
        this.c_ProductName = p_ProductName;
    }

    public String getStoreName() {
        return c_StoreName;
    }

    public String getProductName() {
        return c_ProductName;
    }
}
