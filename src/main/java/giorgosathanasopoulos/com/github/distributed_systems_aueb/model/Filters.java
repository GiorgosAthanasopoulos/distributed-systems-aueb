package giorgosathanasopoulos.com.github.distributed_systems_aueb.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import giorgosathanasopoulos.com.github.distributed_systems_aueb.math.Math;

public class Filters {
    @SerializedName("Latitude")
    private final double c_Latitude;

    @SerializedName("Longitude")
    private final double c_Longitude;

    @SerializedName("RadiusKm")
    private final double c_RadiusKm;

    @SerializedName("FoodType")
    private final List<String> c_FoodTypes;

    @SerializedName("Stars")
    private final List<Integer> c_Stars;

    @SerializedName("Price")
    private final List<Integer> c_Prices;

    public Filters(double p_Latitude, double p_Longitude, double p_RadiusKm, List<String> p_FoodTypes,
            List<Integer> p_Stars,
            List<Integer> p_Prices) {
        this.c_Latitude = p_Latitude;
        this.c_Longitude = p_Longitude;
        this.c_RadiusKm = p_RadiusKm;
        this.c_FoodTypes = p_FoodTypes;
        this.c_Stars = p_Stars;
        this.c_Prices = p_Prices;
    }

    public double getLatitude() {
        return c_Latitude;
    }

    public double getLongitude() {
        return c_Longitude;
    }

    public double getRadiusKm() {
        return c_RadiusKm;
    }

    public List<String> getFoodTypes() {
        return c_FoodTypes;
    }

    public List<Integer> getStars() {
        return c_Stars;
    }

    public List<Integer> getPrices() {
        return c_Prices;
    }

    public boolean abides(Store store) {
        if (!c_FoodTypes.contains(store.getFoodCategory())) {
            System.out.println("food category");
            return false;
        }
        if (!c_Stars.contains(store.getStars())) {
            System.out.println("stars");
            return false;
        }
        if (!c_Prices.contains(store.getInflationIndex())) {
            System.out.println("price");
            return false;
        }
        if (Math.getDistanceFromLatLonInKm(c_Latitude, c_Longitude, store.getLatitude(),
                store.getLongitutde()) > c_RadiusKm) {
            System.out.println("location");
            return false;
        }

        System.out.println("filtered");
        return true;
    }
}
