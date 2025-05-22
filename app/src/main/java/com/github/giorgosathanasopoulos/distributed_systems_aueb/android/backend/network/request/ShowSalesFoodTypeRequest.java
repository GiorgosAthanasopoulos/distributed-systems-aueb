package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;

public class ShowSalesFoodTypeRequest extends Request {

    @SerializedName("FoodType")
    private final String c_FoodType;

    public ShowSalesFoodTypeRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            String p_FoodType) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_FoodType = p_FoodType;
        setSrc(JsonUtils.toJson(this));
    }

    public String getFoodType() {
        return c_FoodType;
    }
}
