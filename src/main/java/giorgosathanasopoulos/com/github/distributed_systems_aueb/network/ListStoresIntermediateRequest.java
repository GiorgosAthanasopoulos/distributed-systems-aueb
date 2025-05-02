package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;

public class ListStoresIntermediateRequest extends Request {
    @SerializedName("Stores")
    private final List<Store> c_STORES;

    public ListStoresIntermediateRequest(int p_Id, List<Store> p_Stores) {
        super(UserAgent.WORKER, p_Id, Action.LIST_STORES);
        c_STORES = p_Stores;
        setSrc(JsonUtils.toJson(this));
    }

    public List<Store> getStores() {
        return c_STORES;
    }
}
