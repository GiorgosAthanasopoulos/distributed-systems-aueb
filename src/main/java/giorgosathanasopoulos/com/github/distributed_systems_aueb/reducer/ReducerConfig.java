package giorgosathanasopoulos.com.github.distributed_systems_aueb.reducer;

public class ReducerConfig {
    // Network settings
    public static final String c_MASTER_HOST = "localhost";
    public static final int c_MASTER_PORT = 8080;

    // Connection settings
    public static final int c_REDUCER_RECONNECT_ATTEMPTS = 5;
    public static final int c_REDUCER_SOCKET_TIMEOUT_MS = 5000;
    public static final int c_REDUCER_HEARTBEAT_INTERVAL_MS = 30000;
}
