package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;

public class Request extends Message {

    @SerializedName("Action")
    private final Action c_Action;

    public Request(UserAgent p_UserAgent, int p_Id, Action p_Action) {
        super(Type.REQUEST, p_Id, p_UserAgent);
        this.c_Action = p_Action;
        setSrc(JsonUtils.toJson(this));
    }

    public Action getAction() {
        return c_Action;
    }

    public enum Action {
        ADD_STORE,
        LIST_STORES,
        FILTER_STORES,

        ADD_PRODUCT,
        LIST_PRODUCTS,
        REMOVE_PRODUCT,
        DECREASE_QUANTITY,
        INCREASE_QUANTITY,

        SHOW_SALES_FOOD_TYPE,
        SHOW_SALES_STORE_TYPE,

        WORKER_HANDSHAKE,
        REDUCER_HANDSHAKE,

        HEARTBEAT,
    }
}
