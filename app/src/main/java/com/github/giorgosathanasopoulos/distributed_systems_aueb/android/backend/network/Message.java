package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;

public class Message {

    @SerializedName("Type")
    private final Type c_Type;

    @SerializedName("UserAgent")
    private final UserAgent c_UserAgent;

    @SerializedName("Id")
    private int c_Id;

    private transient String m_Src;

    public Message(Type p_Type, int p_Id, UserAgent p_UserAgent) {
        this.c_Type = p_Type;
        this.c_UserAgent = p_UserAgent;
        this.c_Id = p_Id;
        m_Src = JsonUtils.toJson(this);
    }

    public Type getType() {
        return c_Type;
    }

    public UserAgent getUserAgent() {
        return c_UserAgent;
    }

    public int getId() {
        return c_Id;
    }

    public void setId(int p_Id) {
        this.c_Id = p_Id;
    }

    public String getSrc() {
        return m_Src;
    }

    public void setSrc(String p_Json) {
        this.m_Src = p_Json;
    }

    public enum Type {
        REQUEST,
        RESPONSE,
    }

    public enum UserAgent {
        MASTER,
        WORKER,
        REDUCER,
        CLIENT,
    }
}
