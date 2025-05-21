// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'filters.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Filters _$FiltersFromJson(Map<String, dynamic> json) => Filters(
  (json['Latitude'] as num).toDouble(),
  (json['Longitude'] as num).toDouble(),
  (json['RadiusKm'] as num).toDouble(),
  (json['FoodType'] as List<dynamic>).map((e) => e as String).toList(),
  (json['Stars'] as List<dynamic>).map((e) => (e as num).toInt()).toList(),
  (json['Price'] as List<dynamic>).map((e) => (e as num).toInt()).toList(),
);

Map<String, dynamic> _$FiltersToJson(Filters instance) => <String, dynamic>{
  'Latitude': instance.c_latitude,
  'Longitude': instance.c_longitude,
  'RadiusKm': instance.c_radiusKm,
  'FoodType': instance.c_foodTypes,
  'Stars': instance.c_stars,
  'Price': instance.c_prices,
};
