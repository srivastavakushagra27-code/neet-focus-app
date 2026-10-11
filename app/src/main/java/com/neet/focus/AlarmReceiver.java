package com.neet.focus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
      Alarms.scheduleAll(c);
      Widgets.refresh(c);
      return;
    }
    String type = i.getStringExtra("type");
    if (type == null) return;
    String t = i.getStringExtra("t");
    String b = i.getStringExtra("b");
    boolean goal = "goal".equals(type);
    if (goal) { b = (t == null ? "" : t) + " - you hit your study goal"; t = "Goal reached"; }
    NotificationManager nm = c.getSystemService(NotificationManager.class);
    nm.createNotificationChannel(new NotificationChannel("alerts", "Reminders and goals", NotificationManager.IMPORTANCE_HIGH));
    Notification n = new Notification.Builder(c, "alerts")
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(t).setContentText(b).setAutoCancel(true)
        .setContentIntent(Alarms.open(c)).build();
    nm.notify(goal ? 2 : i.getIntExtra("id", 3), n);
    if ("rem".equals(type)) Alarms.scheduleAll(c);
  }
}
