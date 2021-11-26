import 'package:flutter/material.dart';
import 'dart:async';

import 'package:you_dl_fl/you_dl_fl.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  YoutubeDlVideoInfo? _data;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    YoutubeDlVideoInfo? data;

    data = await YouDlFl.getStreamInfo("https://youtu.be/Pv61yEcOqpw");

    setState(() {
      _data = data;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Url is: ${_data?.url ?? "null"}\n'),
        ),
      ),
    );
  }
}
