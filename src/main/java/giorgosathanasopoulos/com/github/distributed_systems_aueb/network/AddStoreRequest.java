package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;

public class AddStoreRequest extends Request {

    @SerializedName("Store")
    private final Store c_Store;

    public AddStoreRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action,
            Store p_Store) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_Store = p_Store;
    }

    public Store getStore() {
        return c_Store;
    }
}
