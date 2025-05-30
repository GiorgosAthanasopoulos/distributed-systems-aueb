package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response.Response;

import java.net.Socket;
import java.util.Optional;

public interface TCPClient {
    Socket connectToServer(String p_Host, int p_Port);

    void sendHandshake(Socket p_To);

    void listenForMessages(Socket p_From);

    Optional<Response> handleMessage(String p_Json);

    Optional<Response> handleResponse(String p_Json);

    Optional<Response> handleRequest(String p_Json);
}
