import 'package:android/model/store.dart';
import 'package:android/net/response/response.dart';
import 'package:android/net/common/message.dart';
import 'package:json_annotation/json_annotation.dart';

part 'filter_stores_response.g.dart';

@JsonSerializable()
class FilterStoresResponse extends Response {
  @JsonKey(name: "Stores")
  final List<Store> c_stores;

  FilterStoresResponse(
    super.c_type,
    super.c_userAgent,
    super.c_status,
    super.c_message,
    this.c_stores, {
    super.c_about = About.filterStores,
  });

  List<Store> get stores => c_stores;

  factory FilterStoresResponse.fromJson(Map<String, dynamic> json) =>
      _$FilterStoresResponseFromJson(json);
  Map<String, dynamic> toJson() => _$FilterStoresResponseToJson(this);
}
