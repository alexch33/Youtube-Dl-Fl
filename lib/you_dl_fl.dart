import 'dart:async';

import 'package:flutter/services.dart';

class YouDlFl {
  static const MethodChannel _channel = MethodChannel('you_dl_fl');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  // Get a single playable link containing video+audio
  static Future<String?> getSinglePlayLink(String url) async {
    final String? link =
        await _channel.invokeMethod('getSingleLink', {"url": url});
    return link;
  }

  // getStreamInfo
  static Future<YoutubeDlVideoInfo?> getStreamInfo(String url) async {
    final result = await _channel
        .invokeMethod<Map<dynamic, dynamic>>('getStreamInfo', {"url": url});

    if (result != null) {
      return YoutubeDlVideoInfo.fromMap(result);
    }

    return null;
  }
}

class YoutubeDlVideoInfo {
  final String? title;
  final String? url;
  final Map<dynamic, dynamic>? httpHeaders;
  final int? duration;
  final int? height;
  final int? width;
  final String? format;
  final String? fullTitle;
  final String? thumbnail;
  final String? resolution;

  YoutubeDlVideoInfo(
      {this.title,
      this.url,
      this.httpHeaders,
      this.duration,
      this.height,
      this.width,
      this.format,
      this.fullTitle,
      this.thumbnail,
      this.resolution});

  factory YoutubeDlVideoInfo.fromMap(Map<dynamic, dynamic> json) =>
      YoutubeDlVideoInfo(
        title: json['title'],
        url: json["url"],
        httpHeaders: json['httpHeaders'],
        duration: json['duration'],
        height: json['height'],
        width: json['width'],
        format: json['format'],
        fullTitle: json['fullTitle'],
        thumbnail: json['thumbnail'],
        resolution: json['resolution'],
      );
}
