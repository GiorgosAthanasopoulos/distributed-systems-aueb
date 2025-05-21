// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Response _$ResponseFromJson(Map<String, dynamic> json) => Response(
  $enumDecode(_$TypeEnumMap, json['Type']),
  $enumDecode(_$UserAgentEnumMap, json['UserAgent']),
  $enumDecode(_$StatusEnumMap, json['Status']),
  json['Message'] as String,
  c_about:
      $enumDecodeNullable(_$AboutEnumMap, json['About']) ?? About.defaultValue,
);

Map<String, dynamic> _$ResponseToJson(Response instance) => <String, dynamic>{
  'UserAgent': _$UserAgentEnumMap[instance.c_userAgent]!,
  'Status': _$StatusEnumMap[instance.c_status]!,
  'Message': instance.c_message,
  'About': _$AboutEnumMap[instance.c_about]!,
  'Type': _$TypeEnumMap[instance.c_type]!,
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
