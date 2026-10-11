package com.neet.focus;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import java.io.OutputStream;

class Bridge {
  private final Context c;
  Bridge(Context ctx) { c = ctx.getApplicationContext(); }

  @JavascriptInterface public void sync(String json) {
    c.getSharedPreferences("n", 0).edit().putString("stats", json).apply();
    Widgets.refresh(c);
  }

  @JavascriptInterface public void timer(String status, String label, long elapsed, long goal) {
    TimerService.update(c, status, label, elapsed, goal);
  }

  @JavascriptInterface public void reminders(String json) {
    c.getSharedPreferences("n", 0).edit().putString("rems", json).apply();
    Alarms.scheduleAll(c);
  }

  @JavascriptInterface public boolean saveBackup(String name, String text) {
    if (Build.VERSION.SDK_INT < 29) return false;
    try {
      ContentValues v = new ContentValues();
      v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
      v.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
      v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
      Uri u = c.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
      if (u == null) return false;
      OutputStream o = c.getContentResolver().openOutputStream(u);
      o.write(text.getBytes("UTF-8"));
      o.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
