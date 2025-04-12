package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Filters;

public class FilterStoresRequest extends Request {
    @SerializedName("Filters")
    private final Filters c_Filters;

    public FilterStoresRequest(UserAgent p_UserAgent, int p_Id, String p_Json, Action p_Action, Filters p_Filters) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_Filters = p_Filters;
    }

    public Filters getFilters() {
        return c_Filters;
    }
}
