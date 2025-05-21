import 'package:android/utils/uid_generator.dart';
import 'package:json_annotation/json_annotation.dart';

part 'message.g.dart';

@JsonSerializable()
class Message {
  @JsonKey(name: "Type")
  final Type c_type;
  @JsonKey(name: "UserAgent")
  final UserAgent c_userAgent;
  @JsonKey(name: "Id")
  late int _c_id;

  Message(this.c_type, this.c_userAgent) {
    _c_id = UIDGenerator.next;
  }

  Type get type => c_type;
  UserAgent get userAgent => c_userAgent;
  int get id => _c_id;

  factory Message.fromJson(Map<String, dynamic> json) =>
      _$MessageFromJson(json);
  Map<String, dynamic> toJson() => _$MessageToJson(this);
}

enum Type {
  @JsonValue("REQUEST")
  request,
  @JsonValue("RESPONSE")
  response,
}

enum UserAgent {
  @JsonValue("CLIENT")
  client,
  @JsonValue("MASTER")
  master,
}
