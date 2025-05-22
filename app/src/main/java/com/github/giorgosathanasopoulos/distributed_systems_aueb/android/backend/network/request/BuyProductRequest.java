package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.request;

import com.google.gson.annotations.SerializedName;

public class BuyProductRequest extends Request {

    @SerializedName("StoreName")
    private final String c_STORE_NAME;

    @SerializedName("ProductName")
    private final String c_PRODUCT_NAME;

    @SerializedName("Quantity")
    private final int c_QUANTITY;

    public BuyProductRequest(UserAgent p_UserAgent, int p_Id, Action p_Action,
            String p_StoreName, String p_ProductName, int p_Quantity) {
        super(p_UserAgent, p_Id, p_Action);
        this.c_STORE_NAME = p_StoreName;
        this.c_PRODUCT_NAME = p_ProductName;
        this.c_QUANTITY = p_Quantity;
    }

    public String getStoreName() {
        return c_STORE_NAME;
    }

    public String getProductName() {
        return c_PRODUCT_NAME;
    }

    public int getQuantity() {
        return c_QUANTITY;
    }
}
