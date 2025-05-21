import 'package:android/pages/home_page.dart';
import 'package:flutter/material.dart';

import 'net/backend.dart';

class DistributedSystemsAUEBApp extends StatelessWidget {
  DistributedSystemsAUEBApp({super.key}) {
    Backend.init();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Distributed Systems AUEB',
      home: const HomePage(),
    );
  }
}
