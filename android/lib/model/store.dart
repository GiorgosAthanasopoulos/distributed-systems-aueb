import 'package:android/model/product.dart';
import 'package:android/utils/uid_generator.dart';
import 'package:json_annotation/json_annotation.dart';

part 'store.g.dart';

@JsonSerializable()
class Store {
  @JsonKey(name: "Name")
  final String c_storeName;
  @JsonKey(name: "Latitude")
  final double c_latitude;
  @JsonKey(name: "Longitude")
  final double c_longitude;
  @JsonKey(name: "FoodCategory")
  final String c_foodCategory;
  @JsonKey(name: "Stars")
  int m_stars;
  @JsonKey(name: "NoOfVotes")
  int m_noOfVotes;
  @JsonKey(name: "LogoPath")
  final String c_storeLogo;
  @JsonKey(name: "Products")
  final List<Product> c_products = [];
  @JsonKey(name: "InflationIndex")
  late int m_inflationIndex;
  @JsonKey(name: "Id")
  final int c_id;

  Store(
    this.c_storeName,
    this.c_latitude,
    this.c_longitude,
    this.c_foodCategory,
    this.m_stars,
    this.m_noOfVotes,
    this.c_storeLogo,
  ) : c_id = UIDGenerator.next {
    calculateInflationIndex();
  }

  String get storeName => c_storeName;
  double get latitude => c_latitude;
  double get longitude => c_longitude;
  String get foodCategory => c_foodCategory;
  int get stars => m_stars;
  int get noOfVotes => m_noOfVotes;
  String get storeLogo => c_storeLogo;
  List<Product> get products => c_products;
  int get inflationIndex => m_inflationIndex;
  int get id => c_id;

  bool addProduct(Product p_product) {
    if (c_products.any((Product p) => p.c_name == p_product.c_name)) {
      return false;
    }

    c_products.add(p_product);
    return true;
  }

  bool removeProduct(String p_productName) {
    if (c_products.any((Product p) => p.c_name == p_productName)) {
      c_products
          .firstWhere((Product p) => p.c_name == p_productName)
          .setVisible(false);
      return true;
    }

    return false;
  }

  void calculateInflationIndex() {
    double sum = 0;

    for (Product product in c_products) {
      sum += product.c_price;
    }

    int avg = c_products.isNotEmpty ? sum ~/ c_products.length : 0;
    m_inflationIndex = avg > 15
        ? 3
        : avg > 5
        ? 2
        : 1;
  }

  bool containsProduct(Product p_product) {
    return c_products.any((Product p) => p.c_name == p_product.c_name);
  }

  bool containsProductName(String p_productName) {
    return c_products.any((Product p) => p.c_name == p_productName);
  }

  Product? getProduct(String p_productName) {
    if (!containsProductName(p_productName)) {
      return null;
    }

    return c_products.firstWhere((Product p) => p.c_name == p_productName);
  }

  factory Store.fromJson(Map<String, dynamic> json) => _$StoreFromJson(json);
  Map<String, dynamic> toJson() => _$StoreToJson(this);
}
