import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class YouDlFl {
  static const MethodChannel _channel = MethodChannel('you_dl_fl');
  static const EventChannel _eventChannel = EventChannel('you_dl_fl_events');
  static Function(dynamic data)? downloadCallback;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  // Get a single playable link containing video+audio
  static Future<String?> getSinglePlayLink(String url, String? quality,
      {List<Map<String, String>> arguments = const []}) async {
    final String? link = await _channel.invokeMethod('getSingleLink',
        {"url": url, "quality": quality, "arguments": arguments});
    return link;
  }

  static Future<List<VideoFormat>> getAvailableFormats(String url,
      {List<Map<String, String>> arguments = const []}) async {
    final data = await _channel.invokeMethod(
        'getAvailableFormats', {"url": url, "arguments": arguments});
    Set<VideoFormat> formats = {};

    if (data['exitCode'] > 0) {
      throw (data['error']);
    }

    for (var data in data["out"]) {
      VideoFormat format = VideoFormat.fromMap(data);
      formats.add(format);
    }

    return formats.toList();
  }

  // getStreamInfo
  static Future<YoutubeDlVideoInfo?> getStreamInfo(String url, String? quality,
      {List<Map<String, String>> arguments = const []}) async {
    final result = await _channel.invokeMethod<Map<dynamic, dynamic>>(
        'getStreamInfo',
        {"url": url, "quality": quality, "arguments": arguments});

    if (result != null) {
      return YoutubeDlVideoInfo.fromMap(result);
    }

    return null;
  }

  static Future<String> startDownload(
      String url, String downloadPath, String filename, String? quality,
      {List<Map<String, String>> arguments = const []}) async {
    await Future.delayed(Duration.zero, () {
      var subscription = _eventChannel.receiveBroadcastStream({
        "url": url,
        "path": downloadPath,
        "filename": filename,
        "quality": quality,
        "arguments": arguments
      });
      subscription.listen((event) {
        if (downloadCallback != null) {
          downloadCallback!(event);
        }
      });
    });

    return "$url:$downloadPath/$filename:${quality ?? "best"}";
  }

  static Future<bool> upgradeBinary() async {
    final result = await _channel.invokeMethod<bool>('upgradeBinary');
    return result ?? false;
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

class VideoFormat {
  final String quality;
  final String resolution;
  String format;

  VideoFormat(
      {required this.quality, required this.resolution, required this.format});

  factory VideoFormat.fromMap(Map<dynamic, dynamic> json) => VideoFormat(
      quality: json['quality'],
      resolution: json['resolution'],
      format: json['format']);

  @override
  String toString() {
    return "$format $quality $resolution";
  }

  @override
  int get hashCode => hashValues(quality, format, resolution);

  @override
  operator ==(other) =>
      other is VideoFormat &&
      other.format == format &&
      other.resolution == resolution &&
      other.quality == quality;
}
