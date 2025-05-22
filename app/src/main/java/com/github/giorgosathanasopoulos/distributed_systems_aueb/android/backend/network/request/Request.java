package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.Message;

public class Request extends Message {

    @SerializedName("Action")
    private final Action c_Action;

    public Request(Message.UserAgent p_UserAgent, int p_Id, Action p_Action) {
        super(Type.REQUEST, p_Id, p_UserAgent);
        this.c_Action = p_Action;
        setSrc(JsonUtils.toJson(this));
    }

    public Action getAction() {
        return c_Action;
    }

    public enum Action {
        ADD_STORE,
        LIST_STORES, // stats
        FILTER_STORES, // stats

        ADD_PRODUCT,
        LIST_PRODUCTS,
        REMOVE_PRODUCT,
        DECREASE_QUANTITY,
        INCREASE_QUANTITY,
        BUY_PRODUCT,

        SHOW_SALES_FOOD_TYPE, // stats
        SHOW_SALES_STORE_TYPE, // stats

        WORKER_HANDSHAKE, // handshake
        REDUCER_HANDSHAKE, // handshake

        HEARTBEAT,
    }
}
