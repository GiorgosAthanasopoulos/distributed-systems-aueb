package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.Message;
import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;

public class Response extends Message {

    @SerializedName("Status")
    private final Status c_Status;

    @SerializedName("Message")
    private final String c_Message;

    @SerializedName("About")
    private final About c_About;

    public Response(
            UserAgent p_UserAgent,
            int p_Id,
            Status p_Status,
            String p_Message) {
        super(Type.RESPONSE, p_Id, p_UserAgent);
        this.c_Status = p_Status;
        this.c_Message = p_Message;
        this.c_About = About.DEFAULT;
        setSrc(JsonUtils.toJson(this));
    }

    public Response(
            UserAgent p_UserAgent,
            int p_Id,
            Status p_Status,
            String p_Message,
            About p_About) {
        super(Type.RESPONSE, p_Id, p_UserAgent);
        this.c_Status = p_Status;
        this.c_Message = p_Message;
        this.c_About = p_About;
        setSrc(JsonUtils.toJson(this));
    }

    public enum Status {
        SUCCESS,
        FAILURE,
    }

    public enum About {
        DEFAULT,
        LIST_STORES_REQUEST,
        FILTER_STORES_REQUEST,
        LIST_PRODUCTS_REQUEST,
        SHOW_SALES_FOOD_TYPE_REQUEST,
        SHOW_SALES_STORE_TYPE_REQUEST,
    }

    public Status getStatus() {
        return c_Status;
    }

    public String getMessage() {
        return c_Message;
    }

    public About getAbout() {
        return c_About;
    }
}
