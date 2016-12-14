/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.example.hello;

import org.apache.cordova.*;
import org.json.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Environment;
import android.net.Uri;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends CordovaActivity
{
  private String apkVersion;
  private String apkUrl;
  private String apkName;
  private int googlePlayVersion = 0;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);

      loadUrl(launchUrl);

      new Thread() {
        public void run(){
              checkUpdate();
              handler1.sendEmptyMessage(0);
        }
      }.start();
  }


  private Handler handler1 = new Handler() {
    public void handleMessage(Message msg) {
      if (!apkVersion.equals(getVersion())) {
        showUpdateDialog();
      }
      else return;
    };
  };

  private void checkUpdate() {
    String path = "https://s3.amazonaws.com/stationbuilder-files/apk.json";
    StringBuffer sb = new StringBuffer();
    String line = null;
    BufferedReader reader = null;
    try {
      URL url = new URL(path);
      HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
      urlConnection.getRequestMethod();
      urlConnection.connect();
      reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      while ((line = reader.readLine()) != null) {
        sb.append(line+"\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
        JSONObject info = new JSONObject(sb.toString());
        apkVersion = info.getString("apkVersion");
        apkName = info.getString("apkName");
        apkUrl = info.getString("apkUrl");
    } catch (JSONException e) {
        e.printStackTrace();
    }
  }

  private void showUpdateDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setIcon(android.R.drawable.ic_dialog_info);
    builder.setTitle("Update new version");
    builder.setMessage("new version "+ apkVersion +" is available, would like to update to the new version ");
    builder.setCancelable(false);

    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          downLoadFile(apkUrl);
          //installFromLocal();
        };
    });

    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
        }

    });

    builder.create().show();
  }

  private void downLoadFile(final String url) {

    String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
    String fileName = apkName;
    destination += fileName;
    final Uri uri = Uri.parse("file://" + destination);
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    request.setDescription("Downloading new version");
    request.setTitle("Downloading");
    request.setDestinationUri(uri);
    final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    final ProgressDialog pBar = new ProgressDialog(MainActivity.this);
    final long downloadId = manager.enqueue(request);
    pBar.setTitle("Downloading");
    pBar.show();
    BroadcastReceiver onComplete = new BroadcastReceiver() {
      public void onReceive(Context ctxt, Intent intent) {
        pBar.dismiss();
        try {
          Intent install = new Intent(Intent.ACTION_VIEW);
          install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          install.setDataAndType(uri, "application/vnd.android.package-archive");
          startActivity(install);

        } catch(Exception e) {
          pBar.setMessage(e.getMessage());
          pBar.show();
        }
        unregisterReceiver(this);
      };
    };
    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    return;
  }

  private String getVersion() {
    try {
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                getPackageName(), 0);
        return packageInfo.versionName;
    } catch (NameNotFoundException e) {
        e.printStackTrace();
        return "unknown version";
    }
  }
}
