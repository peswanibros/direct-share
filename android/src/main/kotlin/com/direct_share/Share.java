package com.direct_share;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;

/**
 * Created by zhouteng on 2020/6/27
 * <p>
 * Handles share action
 */
public class Share {

    private static final int CODE_ASK_PERMISSION = 100;

    private final Context context;
    private MethodCall call;

    public Share(Context context) {
        this.context = context;
    }

    void share() {
        share(call);
    }

    void share(MethodCall call) {

        if (call == null) {
            return;
        }

        this.call = call;

        List<String> list = call.argument("list");
        String type = call.argument("type");
        String sharePlatform = call.argument("sharePlatform");
        String sendTo = call.argument("sendTo");
        String sharePanelTitle = call.argument("sharePanelTitle");
        String subject = call.argument("subject");
        String extraText = call.argument("extraText");

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Non-empty list expected");
        }
        if (sharePlatform == null) {
            sharePlatform = "all";
        }
        ArrayList<Uri> uriList = new ArrayList<>();

        Intent shareIntent = new Intent();
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        if ("text".equals(type)) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, list.get(0));
            shareIntent.setType("text/plain");
        } else {
            if (ShareUtils.shouldRequestPermission(list)) {
                if (!checkPermission()) {
                    requestPermission();
                    return;
                }
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
            for (String path : list) {
                File f = new File(path);
                Uri uri = ShareUtils.getUriForFile(context, f);
                uriList.add(uri);
            }

            if ("image".equals(type)) {
                shareIntent.setType("image/*");
            } else if ("video".equals(type)) {
                shareIntent.setType("video/*");
            } else {
                shareIntent.setType("application/*");
            }
            if (uriList.size() == 1) {
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriList.get(0));
            } else {
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            }
        }
        Log.v("params", sendTo + "  " + type + "  " + sharePlatform);
        if (sharePlatform.equalsIgnoreCase("whatsapp")) {
            shareIntent.setPackage("com.whatsapp");
            shareIntent.putExtra("jid", sendTo + "@s.whatsapp.net");//phone number without "+" prefix
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (sharePlatform.equalsIgnoreCase("telegram")) {
            shareIntent.setPackage("org.telegram.messenger");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra("phone", sendTo);//phone number without "+" prefix
            shareIntent.putExtra("jid", sendTo);//phone number without "+" prefix
            shareIntent.putExtra("smsto:", sendTo);//phone number without "+" prefix
            shareIntent.putExtra("to:", sendTo);//phone number without "+" prefix

        }

        if (sharePlatform.equalsIgnoreCase("all")) {
            startChooserActivity(shareIntent, sharePanelTitle, uriList);

        } else {
            context.startActivity(shareIntent);
        }
    }

    private void startChooserActivity(Intent shareIntent, String sharePanelTitle, ArrayList<Uri> uriList) {
        Intent chooserIntent = Intent.createChooser(shareIntent, sharePanelTitle);
        ShareUtils.grantUriPermission(context, uriList, chooserIntent);

        if (context instanceof Activity) {
            context.startActivity(chooserIntent);
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooserIntent);
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (!(context instanceof Activity)) {
            return;
        }
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_ASK_PERMISSION);
    }
}
