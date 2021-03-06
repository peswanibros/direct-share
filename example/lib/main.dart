import 'dart:async';

import 'package:direct_share/direct_share.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await DirectShare.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            TextButton(
              onPressed: () async {
                PickedFile f =
                    await ImagePicker().getImage(source: ImageSource.gallery);

                if (f != null) {
                  DirectShare.share(f.path, "image", "whatsapp", "919876543210",
                      sharePanelTitle: "share image title",
                      extraText: "Hello boiii",
                      subject: "share image subject");
                }
              },
              child: Text("share image"),
            ),
            TextButton(
              onPressed: () async {
                PickedFile f =
                    await ImagePicker().getImage(source: ImageSource.gallery);
                if (f != null) {
                  DirectShare.share(
                      f.path, "image", "telegram", "+919876543210",
                      sharePanelTitle: "share image title",
                      extraText: "Hello boiii",
                      subject: "share image subject");
                }
              },
              child: Text("share image"),
            ),
            TextButton(
              onPressed: () async {
                PickedFile f =
                    await ImagePicker().getImage(source: ImageSource.gallery);
                if (f != null) {
                  DirectShare.share(f.path, "image", "all", "+919876543210",
                      sharePanelTitle: "share image title",
                      extraText: "Hello boiii",
                      subject: "share image subject");
                }
              },
              child: Text("share image"),
            ),
          ],
        ),
      ),
    );
  }
}
