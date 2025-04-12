package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

public class ListStoresRequest extends Request {

    public ListStoresRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
    }
}
