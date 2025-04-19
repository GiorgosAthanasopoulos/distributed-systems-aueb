package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Product;

public class AddProductRequest extends Request {

    @SerializedName("StoreName")
    private final String c_StoreName;

    @SerializedName("Product")
    private final Product c_Product;

    public AddProductRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            String p_StoreName,
            Product p_Product) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_StoreName = p_StoreName;
        this.c_Product = p_Product;
        setSrc(JsonUtils.toJson(this));
    }

    public String getStoreName() {
        return c_StoreName;
    }

    public Product getProduct() {
        return c_Product;
    }
}
