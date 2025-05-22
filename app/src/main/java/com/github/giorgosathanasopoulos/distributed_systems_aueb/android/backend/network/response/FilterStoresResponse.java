package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;

public class FilterStoresResponse extends Response {

    @SerializedName("Stores")
    private final List<Store> c_Stores;

    public FilterStoresResponse(int p_Id, List<Store> p_Stores) {
        super(UserAgent.REDUCER, p_Id, Status.SUCCESS, "List stores result", About.LIST_STORES_REQUEST);
        c_Stores = p_Stores;
        setSrc(JsonUtils.toJson(this));
    }

    public List<Store> getStores() {
        return c_Stores;
    }
}