package com.neet.focus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.util.Locale;

public class TimerService extends Service {
  static void update(Context c, String st, String lb, long el, long gl) {
    if ("ready".equals(st)) {
      c.stopService(new Intent(c, TimerService.class));
      Alarms.cancelGoal(c);
      return;
    }
    try {
      c.startForegroundService(new Intent(c, TimerService.class)
          .putExtra("st", st).putExtra("lb", lb).putExtra("el", el).putExtra("gl", gl));
    } catch (Exception e) { }
  }

  static String fmt(long s) {
    return String.format(Locale.US, "%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
  }

  @Override public IBinder onBind(Intent i) { return null; }

  @Override public int onStartCommand(Intent i, int flags, int startId) {
    if (i == null) { stopSelf(); return START_NOT_STICKY; }
    String st = i.getStringExtra("st");
    String lb = i.getStringExtra("lb");
    long el = i.getLongExtra("el", 0);
    long gl = i.getLongExtra("gl", 0);
    NotificationManager nm = getSystemService(NotificationManager.class);
    nm.createNotificationChannel(new NotificationChannel("timer", "Study timer", NotificationManager.IMPORTANCE_LOW));
    long now = System.currentTimeMillis();
    Notification.Builder b = new Notification.Builder(this, "timer")
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setOngoing(true)
        .setContentTitle("Studying: " + lb)
        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
    if ("running".equals(st)) {
      b.setUsesChronometer(true).setShowWhen(true);
      if (gl > el) {
        long end = now + (gl - el) * 1000;
        b.setWhen(end).setChronometerCountDown(true).setContentText("Time left to your goal");
        Alarms.setGoal(this, end, lb);
      } else {
        b.setWhen(now - el * 1000).setContentText("Elapsed");
        Alarms.cancelGoal(this);
      }
    } else {
      b.setContentText("Paused at " + fmt(el) + (gl > 0 ? " of " + fmt(gl) : ""));
      Alarms.cancelGoal(this);
    }
    startForeground(1, b.build());
    return START_NOT_STICKY;
  }
}
