// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'store.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Store _$StoreFromJson(Map<String, dynamic> json) => Store(
  json['Name'] as String,
  (json['Latitude'] as num).toDouble(),
  (json['Longitude'] as num).toDouble(),
  json['FoodCategory'] as String,
  (json['Stars'] as num).toInt(),
  (json['NoOfVotes'] as num).toInt(),
  json['LogoPath'] as String,
)..m_inflationIndex = (json['InflationIndex'] as num).toInt();

Map<String, dynamic> _$StoreToJson(Store instance) => <String, dynamic>{
  'Name': instance.c_storeName,
  'Latitude': instance.c_latitude,
  'Longitude': instance.c_longitude,
  'FoodCategory': instance.c_foodCategory,
  'Stars': instance.m_stars,
  'NoOfVotes': instance.m_noOfVotes,
  'LogoPath': instance.c_storeLogo,
  'InflationIndex': instance.m_inflationIndex,
};
