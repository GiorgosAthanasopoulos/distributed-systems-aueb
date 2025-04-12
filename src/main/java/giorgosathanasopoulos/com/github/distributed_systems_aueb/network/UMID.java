package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

public class UMID {
    private static int s_Id = 0;

    public static int getNextUniqueMessageId() {
        return s_Id++;
    }
}
