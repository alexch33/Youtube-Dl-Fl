import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
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
  String id = '';
  static const args = [
    {"--add-header": 'Accept-Encoding:"identity;q=1, *;q=0"'},
    {'--add-header': 'Referer:"https://www.imdb.com/video/vi3877612057"'},
    {
      '--add-header':
          'User-Agent:"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36"'
    }
  ];

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
            var dataDir = await getExternalStorageDirectory();

            id = await YouDlFl.startDownload(
                "https://www.imdb.com/video/vi3877612057",
                dataDir!.path,
                "tesst.mp4",
                null,
                arguments: args);

            setState(() {
              _data = id;
            });
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
