import 'package:android/utils/uid_generator.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product.g.dart';

@JsonSerializable()
class Product {
  @JsonKey(name: "Name")
  final String c_name;

  @JsonKey(name: "Type")
  final String c_type;

  @JsonKey(name: "Quantity")
  int m_quantity;

  @JsonKey(name: "Price")
  final double c_price;

  @JsonKey(name: "Visible")
  bool m_visible;

  @JsonKey(name: "Id")
  final int c_id;

  Product(this.c_name, this.c_type, this.m_quantity, this.c_price)
    : c_id = UIDGenerator.next,
      m_visible = true;

  String get name => c_name;
  String get type => c_type;
  int get quantity => m_quantity;
  double get price => c_price;
  bool get visible => m_visible;
  int get id => c_id;

  void setQuantity(int p_quantity) {
    m_quantity = p_quantity;
  }

  void setVisible(bool p_visible) {
    m_visible = p_visible;
  }

  bool decreaseQuantity(int p_amount) {
    if (p_amount <= 0 || p_amount > m_quantity) {
      return false;
    }

    m_quantity -= p_amount;
    return true;
  }

  bool increaseQuantity(int p_amount) {
    if (p_amount <= 0) {
      return false;
    }

    m_quantity += p_amount;
    return true;
  }

  @override
  String toString() {
    String sb = "";
    sb += "{\n";
    sb += "\t\"Name\": \"$c_name\"\n";
    sb += "\t\"Type\": \"$c_type\"\n";
    sb += "\t\"Quantity\": $m_quantity\n";
    sb += "\t\"Price\": $c_price\n";
    sb += "\t\"Visible\": $m_visible\n";
    sb += "},\n";

    return sb;
  }

  factory Product.fromJson(Map<String, dynamic> json) =>
      _$ProductFromJson(json);
  Map<String, dynamic> toJson() => _$ProductToJson(this);
}
