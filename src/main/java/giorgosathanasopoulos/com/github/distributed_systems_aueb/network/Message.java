package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("Type")
    private final Type c_Type;

    @SerializedName("UserAgent")
    private final UserAgent c_UserAgent;

    @SerializedName("ID")
    private int c_Id;

    private transient String m_Src;

    public Message(Type p_Type, int p_Id, UserAgent p_UserAgent, String p_Json) {
        this.c_Type = p_Type;
        this.c_UserAgent = p_UserAgent;
        this.c_Id = p_Id;
        this.m_Src = p_Json;
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

    public void setSrc(String p_json) {
        this.m_Src = p_json;
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
