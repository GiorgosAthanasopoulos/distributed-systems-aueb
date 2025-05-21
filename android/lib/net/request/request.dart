import 'package:android/net/common/message.dart';
import 'package:json_annotation/json_annotation.dart';

part 'request.g.dart';

@JsonSerializable()
class Request extends Message {
  @JsonKey(name: "Action")
  final Action c_action;

  Request(this.c_action) : super(Type.request, UserAgent.client);

  Action get action => c_action;

  factory Request.fromJson(Map<String, dynamic> json) =>
      _$RequestFromJson(json);
  Map<String, dynamic> toJson() => _$RequestToJson(this);
}

enum Action {
  @JsonValue("Action")
  filterStores,
}
