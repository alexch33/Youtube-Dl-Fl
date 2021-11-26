import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:you_dl_fl/you_dl_fl.dart';

void main() {
  const MethodChannel channel = MethodChannel('you_dl_fl');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await YouDlFl.platformVersion, '42');
  });
}
