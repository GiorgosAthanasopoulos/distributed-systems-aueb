// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Product _$ProductFromJson(Map<String, dynamic> json) => Product(
  json['Name'] as String,
  json['Type'] as String,
  (json['Quantity'] as num).toInt(),
  (json['Price'] as num).toDouble(),
)..m_visible = json['Visible'] as bool;

Map<String, dynamic> _$ProductToJson(Product instance) => <String, dynamic>{
  'Name': instance.c_name,
  'Type': instance.c_type,
  'Quantity': instance.m_quantity,
  'Price': instance.c_price,
  'Visible': instance.m_visible,
};
