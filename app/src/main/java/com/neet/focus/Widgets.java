package com.neet.focus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

class Widgets {
  static final int CARD = 0xFF151822, LINE = 0xFF262A3A, PANEL = 0xFF1D2130, ACC = 0xFFC4B5FD, MID = 0xFF6D5FC4, MUT = 0xFFA1A1AA, TXT = 0xFFF4F4F5;

  static void refresh(Context c) {
    AppWidgetManager m = AppWidgetManager.getInstance(c);
    int[] a = m.getAppWidgetIds(new ComponentName(c, OverviewWidget.class));
    if (a.length > 0) update(c, m, a, true);
    int[] b = m.getAppWidgetIds(new ComponentName(c, ExamWidget.class));
    if (b.length > 0) update(c, m, b, false);
  }

  static void update(Context c, AppWidgetManager m, int[] ids, boolean overview) {
    Bitmap bm = overview ? overview(c) : exam(c);
    PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
    for (int id : ids) {
      RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
      rv.setImageViewBitmap(R.id.img, bm);
      rv.setOnClickPendingIntent(R.id.img, pi);
      m.updateAppWidget(id, rv);
    }
  }

  static JSONObject stats(Context c) {
    try {
      JSONObject o = new JSONObject(c.getSharedPreferences("n", 0).getString("stats", "{}"));
      String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
      if (!today.equals(o.optString("date"))) {
        o.put("pct", 0); o.put("mins", 0); o.put("done", 0);
        o.put("subj", new JSONObject());
        JSONArray w = o.optJSONArray("week");
        if (w != null && w.length() == 7) w.put(6, 0);
      }
      return o;
    } catch (Exception e) {
      return new JSONObject();
    }
  }

  static String hm(int m) { return (m / 60) + "h " + (m % 60) + "m"; }

  static Paint tp(int col, float size, boolean bold, Paint.Align al) {
    Paint q = new Paint(Paint.ANTI_ALIAS_FLAG);
    q.setColor(col); q.setTextSize(size);
    q.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    q.setTextAlign(al);
    return q;
  }

  static void rr(Canvas cv, float l, float t, float r, float b, float rad, int col) {
    Paint f = new Paint(Paint.ANTI_ALIAS_FLAG);
    f.setColor(col);
    cv.drawRoundRect(new RectF(l, t, r, b), rad, rad, f);
  }

  static void card(Canvas cv, float w, float h) {
    rr(cv, 0, 0, w, h, 48, CARD);
    Paint s = new Paint(Paint.ANTI_ALIAS_FLAG);
    s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(3); s.setColor(LINE);
    cv.drawRoundRect(new RectF(1.5f, 1.5f, w - 1.5f, h - 1.5f), 48, 48, s);
  }

  static int[] week(JSONObject o) {
    int[] wk = new int[7];
    JSONArray w = o.optJSONArray("week");
    for (int i = 0; i < 7; i++) wk[i] = (w != null && i < w.length()) ? w.optInt(i) : 0;
    return wk;
  }

  static Bitmap overview(Context c) {
    JSONObject o = stats(c);
    int W = 720, H = 420;
    Bitmap bm = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
    Canvas cv = new Canvas(bm);
    card(cv, W, H);
    cv.drawText("Today", 36, 58, tp(MUT, 26, true, Paint.Align.LEFT));
    cv.drawText("NEET Focus", W - 36, 58, tp(MUT, 26, false, Paint.Align.RIGHT));
    String[] v = {o.optInt("pct") + "%", hm(o.optInt("mins")), o.optInt("done") + "/" + o.optInt("total")};
    String[] l = {"Progress", "Studied", "Tasks"};
    for (int i = 0; i < 3; i++) {
      float x = 36 + i * 230;
      cv.drawText(v[i], x, 125, tp(TXT, 46, true, Paint.Align.LEFT));
      cv.drawText(l[i], x, 158, tp(MUT, 24, false, Paint.Align.LEFT));
    }
    int[] wk = week(o);
    double max = Math.max(60, o.optDouble("target", 8) * 60);
    for (int i = 0; i < 7; i++) max = Math.max(max, wk[i]);
    float base = 290, top = 180, bw = 62, gap = (W - 72 - 7 * bw) / 6;
    for (int i = 0; i < 7; i++) {
      float x = 36 + i * (bw + gap);
      float h = Math.max(6f, (float) (wk[i] / max) * (base - top));
      rr(cv, x, base - h, x + bw, base, 14, i == 6 ? ACC : MID);
    }
    JSONObject s = o.optJSONObject("subj");
    if (s == null) s = new JSONObject();
    String[] names = {"Biology", "Chemistry", "Physics", "Revision"};
    int[] cols = {ACC, 0xFF93C5FD, 0xFFF9A8D4, 0xFFFCD34D};
    String[] sh = {"Bio", "Chem", "Phys", "Rev"};
    double tot = 0;
    for (String n : names) tot += s.optDouble(n, 0);
    rr(cv, 36, 318, W - 36, 332, 7, LINE);
    if (tot > 0) {
      float x = 36, full = W - 72;
      for (int i = 0; i < 4; i++) {
        float sw = (float) (s.optDouble(names[i], 0) / tot) * full;
        if (sw > 1) { rr(cv, x, 318, x + sw, 332, 7, cols[i]); x += sw; }
      }
    }
    for (int i = 0; i < 4; i++) {
      float x = 36 + i * 168;
      cv.drawText(hm((int) s.optDouble(names[i], 0)), x, 380, tp(TXT, 26, true, Paint.Align.LEFT));
      cv.drawText(sh[i], x, 406, tp(cols[i], 20, false, Paint.Align.LEFT));
    }
    return bm;
  }

  static int daysLeft(String d) {
    try {
      String[] a = d.split("-");
      Calendar t = Calendar.getInstance();
      t.set(Integer.parseInt(a[0]), Integer.parseInt(a[1]) - 1, Integer.parseInt(a[2]), 0, 0, 0);
      t.set(Calendar.MILLISECOND, 0);
      Calendar n = Calendar.getInstance();
      n.set(Calendar.HOUR_OF_DAY, 0); n.set(Calendar.MINUTE, 0); n.set(Calendar.SECOND, 0); n.set(Calendar.MILLISECOND, 0);
      return (int) Math.round((t.getTimeInMillis() - n.getTimeInMillis()) / 86400000.0);
    } catch (Exception e) {
      return Integer.MIN_VALUE;
    }
  }

  static Bitmap exam(Context c) {
    JSONObject o = stats(c);
    int W = 720, H = 520;
    Bitmap bm = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
    Canvas cv = new Canvas(bm);
    card(cv, W, H);
    String nm = o.optString("examName", "NEET");
    String ed = o.optString("examDate", "");
    cv.drawText(nm.toUpperCase(Locale.ROOT) + " COUNTDOWN", 36, 60, tp(MUT, 26, true, Paint.Align.LEFT));
    cv.drawText(ed, W - 36, 60, tp(MUT, 24, false, Paint.Align.RIGHT));
    double tgt = o.optDouble("target", 8) * 60;
    float f = (float) Math.min(1.0, o.optInt("mins") / tgt);
    rr(cv, 36, 92, W - 36, 148, 28, LINE);
    if (f > 0.02f) rr(cv, 36, 92, 36 + (W - 72) * f, 148, 28, ACC);
    int d = ed.isEmpty() ? Integer.MIN_VALUE : daysLeft(ed);
    String big, sub;
    if (d == Integer.MIN_VALUE) { big = "--"; sub = "Set your exam date in the app"; }
    else if (d > 0) { big = String.valueOf(d); sub = (d == 1 ? "day" : "days") + " to " + nm; }
    else if (d == 0) { big = "Today"; sub = "Exam day. All the best!"; }
    else { big = String.valueOf(-d); sub = "days since " + nm; }
    cv.drawText(big, 36, 285, tp(TXT, 120, true, Paint.Align.LEFT));
    cv.drawText(sub, 36, 330, tp(MUT, 28, false, Paint.Align.LEFT));
    int[] wk = week(o);
    double max = 60;
    for (int i = 0; i < 7; i++) max = Math.max(max, wk[i]);
    float x0 = 430, x1 = W - 48, y0 = 200, y1 = 290, py = 0;
    Path pa = new Path();
    for (int i = 0; i < 7; i++) {
      float x = x0 + i * (x1 - x0) / 6;
      float y = y1 - (float) (wk[i] / max) * (y1 - y0);
      if (i == 0) pa.moveTo(x, y);
      else {
        float px = x0 + (i - 1) * (x1 - x0) / 6;
        pa.cubicTo((px + x) / 2, py, (px + x) / 2, y, x, y);
      }
      py = y;
    }
    Paint st = new Paint(Paint.ANTI_ALIAS_FLAG);
    st.setStyle(Paint.Style.STROKE); st.setStrokeWidth(6); st.setColor(ACC); st.setStrokeCap(Paint.Cap.ROUND);
    cv.drawPath(pa, st);
    cv.drawText("Last 7 days", x0, 330, tp(MUT, 22, false, Paint.Align.LEFT));
    rr(cv, 36, 370, W - 36, 490, 32, PANEL);
    String[] v = {hm(o.optInt("mins")), o.optInt("done") + "/" + o.optInt("total"), o.optInt("streak") + "d"};
    String[] l = {"Studied today", "Tasks done", "Day streak"};
    for (int i = 0; i < 3; i++) {
      float x = 64 + i * 215;
      cv.drawText(v[i], x, 430, tp(TXT, 36, true, Paint.Align.LEFT));
      cv.drawText(l[i], x, 466, tp(MUT, 22, false, Paint.Align.LEFT));
    }
    return bm;
  }
}
