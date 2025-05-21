// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'filter_stores_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FilterStoresResponse _$FilterStoresResponseFromJson(
  Map<String, dynamic> json,
) => FilterStoresResponse(
  $enumDecode(_$TypeEnumMap, json['Type']),
  $enumDecode(_$UserAgentEnumMap, json['UserAgent']),
  $enumDecode(_$StatusEnumMap, json['Status']),
  json['Message'] as String,
  (json['Stores'] as List<dynamic>)
      .map((e) => Store.fromJson(e as Map<String, dynamic>))
      .toList(),
  c_about:
      $enumDecodeNullable(_$AboutEnumMap, json['About']) ?? About.filterStores,
);

Map<String, dynamic> _$FilterStoresResponseToJson(
  FilterStoresResponse instance,
) => <String, dynamic>{
  'Type': _$TypeEnumMap[instance.c_type]!,
  'UserAgent': _$UserAgentEnumMap[instance.c_userAgent]!,
  'Status': _$StatusEnumMap[instance.c_status]!,
  'Message': instance.c_message,
  'About': _$AboutEnumMap[instance.c_about]!,
  'Stores': instance.c_stores,
};

const _$TypeEnumMap = {Type.request: 'REQUEST', Type.response: 'RESPONSE'};

const _$UserAgentEnumMap = {
  UserAgent.client: 'CLIENT',
  UserAgent.master: 'MASTER',
};

const _$StatusEnumMap = {Status.success: 'SUCCESS', Status.failure: 'FAILURE'};

const _$AboutEnumMap = {
  About.defaultValue: 'DEFAULT',
  About.filterStores: 'FILTER_STORES_REQUEST',
};
