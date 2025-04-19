package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.json.JsonUtils;

public class ShowSalesFoodTypeRequest extends Request {

    @SerializedName("FoodType")
    private final String c_FoodType;

    public ShowSalesFoodTypeRequest(
            UserAgent p_UserAgent,
            int p_Id,
            Action p_Action,
            String p_FoodType) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_FoodType = p_FoodType;
        setSrc(JsonUtils.toJson(this));
    }

    public String getFoodType() {
        return c_FoodType;
    }
}
