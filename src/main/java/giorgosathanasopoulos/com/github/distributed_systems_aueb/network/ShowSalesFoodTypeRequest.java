package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

public class ShowSalesFoodTypeRequest extends Request {

    @SerializedName("FoodType")
    private final String c_FoodType;

    public ShowSalesFoodTypeRequest(
            UserAgent p_UserAgent,
            int p_Id,
            String p_Json,
            Action p_Action,
            String p_FoodType) {
        super(p_UserAgent, p_Id, p_Json, p_Action);
        this.c_FoodType = p_FoodType;
    }

    public String getFoodType() {
        return c_FoodType;
    }
}
