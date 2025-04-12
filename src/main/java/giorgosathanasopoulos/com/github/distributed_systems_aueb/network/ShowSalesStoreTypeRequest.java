package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class ShowSalesStoreTypeRequest extends Request {

    @SerializedName("StoreType")
    private final String c_StoreType;

    public ShowSalesStoreTypeRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action,
            String p_StoreType) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_StoreType = p_StoreType;
    }

    public String getStoreType() {
        return c_StoreType;
    }
}
