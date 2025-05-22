package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.network.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.json.JsonUtils;
import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model.Product;

public class ListProductsResponse extends Response {
    @SerializedName("StoreName")
    private final String c_StoreName; // for ease of use

    @SerializedName("Products")
    private final List<Product> c_Products;

    public ListProductsResponse(int p_Id, String p_StoreName, List<Product> p_Products) {
        super(UserAgent.WORKER, p_Id, Status.SUCCESS, "", About.LIST_PRODUCTS_REQUEST);
        this.c_StoreName = p_StoreName;
        this.c_Products = p_Products;
        setSrc(JsonUtils.toJson(this));
    }

    public String getStoreName() {
        return c_StoreName;
    }

    public List<Product> getProducts() {
        return c_Products;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(c_StoreName).append(": \n");
        for (Product product : c_Products) {
            sb.append(product.toString());
        }

        return sb.toString();
    }
}
