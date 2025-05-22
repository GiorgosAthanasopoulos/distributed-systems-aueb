package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Filters;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;

public class FilterStoresIntermediateRequest extends Request {
    @SerializedName("Stores")
    private final List<Store> c_STORES;

    @SerializedName("Filters")
    private final Filters c_FILTERS;

    public FilterStoresIntermediateRequest(int p_Id, List<Store> p_Stores, Filters p_Filters) {
        super(UserAgent.WORKER, p_Id, Action.FILTER_STORES);
        c_STORES = p_Stores;
        c_FILTERS = p_Filters;
        setSrc(JsonUtils.toJson(this));
    }

    public List<Store> getStores() {
        return c_STORES;
    }

    public Filters getFilters() {
        return c_FILTERS;
    }
}
