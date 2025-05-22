package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;

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
