package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Store;

public class AddStoreRequest extends Request {

    @SerializedName("Store")
    private final Store c_Store;

    public AddStoreRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            Store p_Store) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_Store = p_Store;
        setSrc(JsonUtils.toJson(this));
    }

    public Store getStore() {
        return c_Store;
    }
}
