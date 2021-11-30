package com.i3po.you_dl.you_dl_fl;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;

/** YouDlFlPlugin */
public class YouDlFlPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware {
  private static final String TAG = "YouDlFlPlugin";
  private static boolean isInited;
  private final Handler handler = new Handler();
  Map<Integer, String> keysMeaning = new HashMap<>();


  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private Context context;

  private Map<Object, Runnable> listeners = new HashMap<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "you_dl_fl");
    channel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "you_dl_fl_events");
    eventChannel.setStreamHandler(this);

    initializeKeysMeanings();
    initializeYouDl(flutterPluginBinding.getApplicationContext());
  }

    private void initializeKeysMeanings() {
        keysMeaning.put(0, "quality");
        keysMeaning.put(1, "format");
        keysMeaning.put(2, "resolution");
    }

    private void initializeYouDl(Context applicationContext) {
    try {
      YoutubeDL.getInstance().init(applicationContext);
      FFmpeg.getInstance().init(applicationContext);
      context = applicationContext;
      isInited = true;
    } catch (YoutubeDLException e) {
      isInited = false;
      Log.e(TAG, "failed to initialize youtubedl-android", e);
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "getStreamInfo":
        handleRequestStreamInfo(result, call);
        break;
      case "getSingleLink":
        handleGetSingleLink(result, call);
        break;
        case "getAvailableFormats":
            handleGetAvailableFormats(result, call);
            break;
        case "upgradeBinary":
            upgradeBinary(result);
            break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void upgradeBinary(Result result) {
      AsyncTask.execute(() -> {
          try {
              YoutubeDL.getInstance().updateYoutubeDL(context);
              handler.post(() -> result.success(true));
          } catch (YoutubeDLException e) {
              handler.post(() -> result.success(false));
              e.printStackTrace();
          }
      });
  }
    private void handleGetAvailableFormats(Result result, MethodCall call) {
        AsyncTask.execute(() -> {
            String url = call.argument("url");

            YoutubeDLRequest request = new YoutubeDLRequest(url);
            request.addOption("-F", url);
            try {
                YoutubeDLResponse response =  YoutubeDL.getInstance().execute(request);

                Map<String, Object> resultData = new HashMap<>();
                String outStaff = response.getOut();
                List<Map<String, Object>> availableFormats = new ArrayList<>();
                
                for (String line : outStaff.split("\\n")) {

                    if (line.matches(".*\\d{3}x\\d{3}.*") && !line.contains("audio only") && !line.contains("video only")) {
                        String[] lineValues = line.split(" +");
                        Map<String, Object> outData = new HashMap<>();
                        int index = 0;
                        for (String a : lineValues) {
                            a = a.trim();
                            if (a.length() > 0) {
                                if (index > 2) continue;
                                outData.put(keysMeaning.get(index), a);
                                index += 1;
                            }

                        }
                        availableFormats.add(outData);
                    }
                }


                resultData.put("out", availableFormats);
                resultData.put("exitCode", response.getExitCode());
                resultData.put("error", response.getErr());

                handler.post(() -> result.success(resultData));
            } catch (YoutubeDLException | InterruptedException e) {
                handler.post(() -> result.success(null));
                e.printStackTrace();
            }
        });
    }


  private void handleRequestStreamInfo(Result result, MethodCall call) {
    AsyncTask.execute(() -> {
        String url = call.argument("url");
        String format = call.argument("format");

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        if (format == null) {
            request.addOption("-f", "best");
        } else {
            request.addOption("-f", format);
        }
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

            handler.post(() -> result.success(resultData));
        } catch (YoutubeDLException | InterruptedException e) {
            handler.post(() -> result.success(null));
            e.printStackTrace();
        }
    });
  }

  private void handleGetSingleLink(Result result, MethodCall call) {
    AsyncTask.execute(() -> {
        String url = call.argument("url");
        String format = call.argument("format");

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        if (format == null) {
            request.addOption("-f", "best");
        } else {
            request.addOption("-f", format);
        }
        VideoInfo streamInfo = null;
        try {
            streamInfo = YoutubeDL.getInstance().getInfo(request);
            VideoInfo finalStreamInfo = streamInfo;
            handler.post(() -> result.success(finalStreamInfo.getUrl()));
        } catch (YoutubeDLException | InterruptedException e) {
            handler.post(() -> result.success(null));
            e.printStackTrace();
        }
    });
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

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    startListening(arguments, events);
  }

  @Override
  public void onCancel(Object arguments) {
    cancelListening(arguments);
  }

    void startListening(Object listener, EventChannel.EventSink emitter) {
        final Handler handler = new Handler();
        listeners.put(listener, () -> {
            if (listeners.containsKey(listener)) {
                AsyncTask.execute(() -> handleDownload(listener, emitter));
            }
        });
        handler.post(listeners.get(listener));
    }

    private void handleDownload(Object arguments, EventChannel.EventSink events) {
        AsyncTask.execute(() -> {
            HashMap<String, String> args = (HashMap<String, String>) arguments;

            String url = args.get("url");
            String path = args.get("path");
            String filename = args.get("filename");

            if (url == null || path == null || filename == null) {
                cancelListening(arguments);
                return;
            }
            File youtubeDLDir = new File(path);

            YoutubeDLRequest request = new YoutubeDLRequest(url);
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + File.pathSeparator + filename);
            request.addOption("-f", "best");

            try {
                YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
                    HashMap<String, String> data = new HashMap<>();

                    data.put("progress", String.valueOf(progress));
                    data.put("eta", String.valueOf(etaInSeconds));

                    handler.post(() -> events.success(data));
                });
            } catch (YoutubeDLException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void cancelListening(Object arguments) {
    listeners.remove(arguments);
  }
}
