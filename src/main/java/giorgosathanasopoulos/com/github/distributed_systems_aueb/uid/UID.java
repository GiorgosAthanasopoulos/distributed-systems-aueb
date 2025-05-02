package giorgosathanasopoulos.com.github.distributed_systems_aueb.uid;

// maybe have a uid class for every context: master, client, worker, reducer, etc.
// 2 different db tables dont need to have different ids between their records...
public class UID {
    private static int s_Id = 0;

    public static int getNextUID() {
        return s_Id++;
    }
}
