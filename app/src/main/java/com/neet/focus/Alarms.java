package com.neet.focus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;

class Alarms {
  static PendingIntent pi(Context c, String type, int id, String t, String b) {
    Intent i = new Intent(c, AlarmReceiver.class).setAction("neet." + type + id)
        .putExtra("type", type).putExtra("id", id).putExtra("t", t).putExtra("b", b);
    return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
  }

  static PendingIntent open(Context c) {
    return PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
  }

  static void setGoal(Context c, long at, String label) {
    AlarmManager am = c.getSystemService(AlarmManager.class);
    am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, open(c)), pi(c, "goal", 1, label, ""));
  }

  static void cancelGoal(Context c) {
    c.getSystemService(AlarmManager.class).cancel(pi(c, "goal", 1, "", ""));
  }

  static long next(String hm, boolean onceOnly) {
    try {
      String[] a = hm.split(":");
      Calendar k = Calendar.getInstance();
      k.set(Calendar.HOUR_OF_DAY, Integer.parseInt(a[0]));
      k.set(Calendar.MINUTE, Integer.parseInt(a[1]));
      k.set(Calendar.SECOND, 0);
      k.set(Calendar.MILLISECOND, 0);
      if (k.getTimeInMillis() <= System.currentTimeMillis() + 1000) {
        if (onceOnly) return -1;
        k.add(Calendar.DAY_OF_YEAR, 1);
      }
      return k.getTimeInMillis();
    } catch (Exception e) {
      return -1;
    }
  }

  static void scheduleAll(Context c) {
    SharedPreferences p = c.getSharedPreferences("n", 0);
    AlarmManager am = c.getSystemService(AlarmManager.class);
    String old = p.getString("sched", "");
    for (String x : old.split(",")) {
      if (!x.isEmpty()) am.cancel(pi(c, "rem", Integer.parseInt(x), "", ""));
    }
    StringBuilder sb = new StringBuilder();
    try {
      JSONArray a = new JSONArray(p.getString("rems", "[]"));
      for (int k = 0; k < a.length(); k++) {
        JSONObject r = a.getJSONObject(k);
        int id = Math.abs(r.getString("id").hashCode() % 100000) + 100;
        long t = next(r.getString("time"), "once".equals(r.optString("repeat")));
        if (t < 0) continue;
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(t, open(c)),
            pi(c, "rem", id, r.optString("name"), r.optString("notes")));
        sb.append(id).append(',');
      }
    } catch (Exception e) { }
    p.edit().putString("sched", sb.toString()).apply();
  }
}
