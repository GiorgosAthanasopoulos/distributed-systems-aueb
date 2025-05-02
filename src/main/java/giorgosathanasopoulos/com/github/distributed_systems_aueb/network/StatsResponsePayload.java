package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StatsResponsePayload {
    private final Socket c_SOCKET;
    private final int c_RESPONSE_COUNT; // amount of responses to gather to consider payload filled
    private final List<String> c_Responses;

    public StatsResponsePayload(Socket p_To, int p_ResponseCount) {
        c_SOCKET = p_To;
        c_RESPONSE_COUNT = p_ResponseCount;
        c_Responses = new ArrayList<>(3);
    }

    public boolean addResponse(String p_Json) {
        if (isReady())
            return false;

        c_Responses.addLast(p_Json);
        return true;
    }

    public boolean isReady() {
        return c_Responses.size() == c_RESPONSE_COUNT;
    }

    public Socket getSocket() {
        return c_SOCKET;
    }

    public List<String> getResponses() {
        return c_Responses;
    }

    public int getResponseCount() {
        return c_RESPONSE_COUNT;
    }
}
