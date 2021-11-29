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
            setState(() {
              _data = "Loading...";
            });
            String? link = await YouDlFl.getSinglePlayLink(
                "https://www.dailymotion.com/video/x85wubx?playlist=x6lgtp");
            if (link != null) {
              setState(() {
                _data = link;
              });
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
