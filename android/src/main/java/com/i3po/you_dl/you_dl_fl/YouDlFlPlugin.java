package com.i3po.you_dl.you_dl_fl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;

/** YouDlFlPlugin */
public class YouDlFlPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private static final String TAG = "YouDlFlPlugin";
  private static boolean isInited;

  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "you_dl_fl");
    channel.setMethodCallHandler(this);
    initializeYouDl(flutterPluginBinding.getApplicationContext());
  }

  private void initializeYouDl(Context applicationContext) {
    try {
      YoutubeDL.getInstance().init(applicationContext);
      isInited = true;
    } catch (YoutubeDLException e) {
      isInited = false;
      Log.e(TAG, "failed to initialize youtubedl-android", e);
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("getStreamInfo")) {
      handleRequestStreamInfo(result, call);
    } else if (call.method.equals("getSingleLink")) {
      handleGetSingleLink(result, call);
    } else {
      result.notImplemented();
    }
  }

  private void handleRequestStreamInfo(Result result, MethodCall call) {
    String url = call.argument("url");

    YoutubeDLRequest request = new YoutubeDLRequest(url);
    request.addOption("-f", "best");
    VideoInfo streamInfo = null;
    try {
      streamInfo = YoutubeDL.getInstance().getInfo(request);
      Map<String, Object> resultData = new HashMap<>();

      resultData.put("title", streamInfo.getTitle());
      resultData.put("url", streamInfo.getUrl());
      resultData.put("httpHeaders", streamInfo.getHttpHeaders());
      resultData.put("duration", streamInfo.getDuration());
      resultData.put("height", streamInfo.getHeight());
      resultData.put("width", streamInfo.getWidth());
      resultData.put("format", streamInfo.getFormat());
      resultData.put("fullTitle", streamInfo.getFulltitle());
      resultData.put("thumbnail", streamInfo.getThumbnail());
      resultData.put("resolution", streamInfo.getResolution());

      // JSONObject jsonResult = new JSONObject(resultData);

      result.success(resultData);
    } catch (YoutubeDLException | InterruptedException e) {
      result.success(null);
      e.printStackTrace();
    }
  }

  private void handleGetSingleLink(Result result, MethodCall call) {
    String url = call.argument("url");

    YoutubeDLRequest request = new YoutubeDLRequest(url);
    request.addOption("-f", "best");
    VideoInfo streamInfo = null;
    try {
      streamInfo = YoutubeDL.getInstance().getInfo(request);
      result.success(streamInfo.getUrl());
    } catch (YoutubeDLException | InterruptedException e) {
      result.success(null);
      e.printStackTrace();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    // TODO: your plugin is now attached to an Activity
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // TODO: the Activity your plugin was attached to was destroyed to change configuration.
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    // TODO: your plugin is now attached to a new Activity after a configuration change.
  }

  @Override
  public void onDetachedFromActivity() {
    // TODO: your plugin is no longer associated with an Activity. Clean up references.
  }
}
