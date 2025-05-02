package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;

public class ShowSalesStoreTypeIntermediateRequest extends Request {

    @SerializedName("Stores")
    private final List<Store> c_STORES;

    @SerializedName("StoreType")
    private final String c_STORE_TYPE;

    public ShowSalesStoreTypeIntermediateRequest(int p_Id, List<Store> p_Stores, String p_StoreType) {
        super(UserAgent.WORKER, p_Id, Action.SHOW_SALES_STORE_TYPE);
        c_STORES = p_Stores;
        c_STORE_TYPE = p_StoreType;
    }

    public List<Store> getStores() {
        return c_STORES;
    }

    public String getStoreType() {
        return c_STORE_TYPE;
    }
}
