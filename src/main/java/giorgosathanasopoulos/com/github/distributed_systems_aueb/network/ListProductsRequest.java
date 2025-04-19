package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;

public class ListProductsRequest extends Request {

    @SerializedName("StoreName")
    private final String c_StoreName;

    public ListProductsRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            String p_StoreName) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_StoreName = p_StoreName;
        setSrc(JsonUtils.toJson(this));
    }

    public String getStoreName() {
        return c_StoreName;
    }
}
