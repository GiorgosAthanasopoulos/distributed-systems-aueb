import 'package:android/model/store.dart';
import 'package:android/utils/math_utils.dart';
import 'package:json_annotation/json_annotation.dart';

part 'filters.g.dart';

@JsonSerializable()
class Filters {
  @JsonKey(name: "Latitude")
  final double c_latitude;
  @JsonKey(name: "Longitude")
  final double c_longitude;
  @JsonKey(name: "RadiusKm")
  final double c_radiusKm;
  @JsonKey(name: "FoodType")
  final List<String> c_foodTypes;
  @JsonKey(name: "Stars")
  final List<int> c_stars;
  @JsonKey(name: "Price")
  final List<int> c_prices;

  Filters(
    this.c_latitude,
    this.c_longitude,
    this.c_radiusKm,
    this.c_foodTypes,
    this.c_stars,
    this.c_prices,
  );

  double get latitude => c_latitude;
  double get longitude => c_longitude;
  double get radiusKm => c_radiusKm;
  List<String> get foodTypes => c_foodTypes;
  List<int> get stars => c_stars;
  List<int> get prices => c_prices;

  bool abides(Store store) {
    if (!c_foodTypes.contains(store.c_foodCategory)) {
      return false;
    }
    if (!c_stars.contains(store.m_stars)) {
      return false;
    }
    if (!c_prices.contains(store.m_inflationIndex)) {
      return false;
    }
    if (getDistanceFromLatLonInKm(
          c_latitude,
          c_longitude,
          store.c_latitude,
          store.c_longitude,
        ) >
        c_radiusKm) {
      return false;
    }

    return true;
  }

  factory Filters.fromJson(Map<String, dynamic> json) =>
      _$FiltersFromJson(json);
  Map<String, dynamic> toJson() => _$FiltersToJson(this);
}
