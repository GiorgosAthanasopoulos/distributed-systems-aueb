package giorgosathanasopoulos.com.github.distributed_systems_aueb.network;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.model.Store;

public class ShowSalesFoodTypeIntermediateRequest extends Request {

    @SerializedName("Stores")
    private final List<Store> c_STORES;

    @SerializedName("FoodType")
    private final String c_FOOD_TYPE;

    public ShowSalesFoodTypeIntermediateRequest(int p_Id, List<Store> p_Stores, String p_FoodType) {
        super(UserAgent.WORKER, p_Id, Action.SHOW_SALES_FOOD_TYPE);
        c_STORES = p_Stores;
        c_FOOD_TYPE = p_FoodType;
    }

    public List<Store> getStores() {
        return c_STORES;
    }

    public String getFoodType() {
        return c_FOOD_TYPE;
    }
}
