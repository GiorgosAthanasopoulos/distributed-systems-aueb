import 'dart:io';

import 'package:android/net/config.dart';
import 'package:android/net/request/request.dart';
import 'package:android/net/response/response.dart';

class Backend {
  static late Socket _s_ss;

  static void init() {
    Socket.connect(
      g_serverHost,
      g_serverPort,
    ).then((Socket p_ss) => {Backend._s_ss = p_ss});
  }

  // Response sendRequest(Request p_request) {
  //   _s_ss.writeln(p_request.toJson());
  //   _s_ss.flush();
  // }
}
