import 'package:android/model/filters.dart';
import 'package:android/net/request/request.dart';
import 'package:json_annotation/json_annotation.dart';

part 'filter_stores_request.g.dart';

@JsonSerializable()
class FilterStoresRequest extends Request {
  @JsonKey(name: "Filters")
  final Filters c_filters;

  FilterStoresRequest(this.c_filters) : super(Action.filterStores);

  Filters get filters => c_filters;

  factory FilterStoresRequest.fromJson(Map<String, dynamic> json) =>
      _$FilterStoresRequestFromJson(json);
  Map<String, dynamic> toJson() => _$FilterStoresRequestToJson(this);
}
