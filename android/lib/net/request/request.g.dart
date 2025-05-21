// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Request _$RequestFromJson(Map<String, dynamic> json) =>
    Request($enumDecode(_$ActionEnumMap, json['Action']));

Map<String, dynamic> _$RequestToJson(Request instance) => <String, dynamic>{
  'Action': _$ActionEnumMap[instance.c_action]!,
};

const _$ActionEnumMap = {Action.filterStores: 'Action'};
