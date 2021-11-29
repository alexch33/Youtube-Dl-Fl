import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:path_provider/path_provider.dart';
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
  String? _data;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    YouDlFl.downloadCallback = (data) {
      setState(() {
        _data = data["progress"];
      });
    };
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            Directory? dir = await getExternalStorageDirectory();
            if (dir != null) {
              YouDlFl.startDownload(
                  "https://vimeo.com/22439234", dir.path, "tst.mp4");
            }
          },
        ),
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Url is: ${_data ?? "null"}\n'),
        ),
      ),
    );
  }
}
