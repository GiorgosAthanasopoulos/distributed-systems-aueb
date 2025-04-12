package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class Response extends Message {

    @SerializedName("Status")
    private final Status c_Status;

    @SerializedName("Message")
    private final String c_Message;

    public Response(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Status p_Status,
            String p_Message) {
        super(Type.RESPONSE, p_Id, p_UserAgent, p_Json);
        this.c_Status = p_Status;
        this.c_Message = p_Message;
    }

    public enum Status {
        SUCCESS,
        FAILURE,
    }

    public Status getStatus() {
        return c_Status;
    }

    public String getMessage() {
        return c_Message;
    }
}
