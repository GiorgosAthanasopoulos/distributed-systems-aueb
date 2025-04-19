package giorgosathanasopoulos.com.github.distributed_systems_aueb.uid;

public class UID {
    private static int s_Id = 0;

    public static int getNextUID() {
        return s_Id++;
    }
}
