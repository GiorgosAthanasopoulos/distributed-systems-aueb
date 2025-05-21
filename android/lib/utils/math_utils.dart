import 'dart:math';

double getDistanceFromLatLonInKm(
  double p_lat1,
  double p_lon1,
  double p_lat2,
  double p_lon2,
) {
  double R = 6371;

  double dLat = deg2rad(p_lat2 - p_lat1);
  double dLon = deg2rad(p_lon2 - p_lon1);

  double a =
      sin(dLat / 2) * sin(dLat / 2) +
      cos(deg2rad(p_lat1)) *
          cos(deg2rad(p_lat2)) *
          sin(dLon / 2) *
          sin(dLon / 2);

  double c = 2 * atan2(sqrt(a), sqrt(1 - a));
  double d = R * c;
  return d;
}

double deg2rad(double p_degrees) {
  return p_degrees * (pi / 180);
}
