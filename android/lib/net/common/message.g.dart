// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'message.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Message _$MessageFromJson(Map<String, dynamic> json) => Message(
  $enumDecode(_$TypeEnumMap, json['Type']),
  $enumDecode(_$UserAgentEnumMap, json['UserAgent']),
);

Map<String, dynamic> _$MessageToJson(Message instance) => <String, dynamic>{
  'Type': _$TypeEnumMap[instance.c_type]!,
  'UserAgent': _$UserAgentEnumMap[instance.c_userAgent]!,
};

const _$TypeEnumMap = {Type.request: 'REQUEST', Type.response: 'RESPONSE'};

const _$UserAgentEnumMap = {
  UserAgent.client: 'CLIENT',
  UserAgent.master: 'MASTER',
};
