import 'package:android/net/common/message.dart';
import 'package:json_annotation/json_annotation.dart';

part 'response.g.dart';

@JsonSerializable()
class Response extends Message {
  @JsonKey(name: "Status")
  final Status c_status;
  @JsonKey(name: "Message")
  final String c_message;
  @JsonKey(name: "About")
  final About c_about;

  Response(
    super.c_type,
    super.c_userAgent,
    this.c_status,
    this.c_message, {
    this.c_about = About.defaultValue,
  });

  Status get status => c_status;
  String get message => c_message;
  About get about => c_about;

  set c_type(Type p_type) => c_type = p_type;
}

enum Status {
  @JsonValue("SUCCESS")
  success,
  @JsonValue("FAILURE")
  failure,
}

enum About {
  @JsonValue("DEFAULT")
  defaultValue,
  @JsonValue("FILTER_STORES_REQUEST")
  filterStores,
}
