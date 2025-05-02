package giorgosathanasopoulos.com.github.distributed_systems_aueb.math;

public class Math {
    // taken from stack overflow:
    // https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    public static final double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;

        double dLat = deg2rad(lat2 - lat1);
        double dLon = deg2rad(lon2 - lon1);

        double a = java.lang.Math.sin(dLat / 2) * java.lang.Math.sin(dLat / 2) +
                java.lang.Math.cos(deg2rad(lat1)) * java.lang.Math.cos(deg2rad(lat2)) *
                        java.lang.Math.sin(dLon / 2) * java.lang.Math.sin(dLon / 2);

        double c = 2 * java.lang.Math.atan2(java.lang.Math.sqrt(a), java.lang.Math.sqrt(1 - a));
        double d = R * c;
        return d;
    }

    public static final double deg2rad(double p_Degrees) {
        return p_Degrees * (java.lang.Math.PI / 180);
    }
}