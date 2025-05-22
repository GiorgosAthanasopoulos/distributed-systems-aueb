package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;

public class ShowSalesStoreTypeRequest extends Request {

    @SerializedName("StoreType")
    private final String c_StoreType;

    public ShowSalesStoreTypeRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            String p_StoreType) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_StoreType = p_StoreType;
        setSrc(JsonUtils.toJson(this));
    }

    public String getStoreType() {
        return c_StoreType;
    }
}
