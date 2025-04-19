package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;
import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;

public class FilterStoresRequest extends Request {
    @SerializedName("Filters")
    private final Filters c_Filters;

    public FilterStoresRequest(UserAgent p_UserAgent, int p_Id, Action p_Action, Filters p_Filters) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_Filters = p_Filters;
        setSrc(JsonUtils.toJson(this));
    }

    public Filters getFilters() {
        return c_Filters;
    }
}
