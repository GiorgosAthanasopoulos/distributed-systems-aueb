package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Filters;

public class FilterStoresRequest extends Request {
    @SerializedName("Filters")
    private final Filters c_Filters;

    public FilterStoresRequest(UserAgent p_UserAgent, int p_Id, Filters p_Filters) {
        super(p_UserAgent, p_Id, Action.FILTER_STORES);
        this.c_Filters = p_Filters;
        setSrc(JsonUtils.toJson(this));
    }

    public Filters getFilters() {
        return c_Filters;
    }
}
