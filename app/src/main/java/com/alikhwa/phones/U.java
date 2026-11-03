package com.alikhwa.phones;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class U {
    public static final int PRIMARY = 0xFF0D47A1, ACCENT = 0xFFFF6F00, BG = 0xFFF3F5F9, TEXT = 0xFF1B1F2A,
            GRAY = 0xFF6B7280, WHITE = 0xFFFFFFFF, RED = 0xFFD32F2F, GREEN = 0xFF2E7D32;
    public static final ExecutorService EX = Executors.newFixedThreadPool(3);
    public static final Handler H = new Handler(Looper.getMainLooper());
    static final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(24 * 1024 * 1024) {
        @Override protected int sizeOf(String k, Bitmap b) { return b.getByteCount(); }
    };

    public static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }

    public static void rtl(Activity a) { a.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL); }

    public static GradientDrawable bg(Context c, int color, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, r));
        return g;
    }

    public static GradientDrawable bgStroke(Context c, int color, float r, int stroke) {
        GradientDrawable g = bg(c, color, r);
        g.setStroke(dp(c, 1), stroke);
        return g;
    }

    public static TextView tv(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    public static TextView btn(Context c, String s, int color) {
        TextView t = tv(c, s, 15, WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12));
        t.setBackground(bg(c, color, 12));
        t.setClickable(true);
        return t;
    }

    public static EditText et(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextSize(15);
        e.setTextColor(TEXT);
        e.setHintTextColor(0xFF9CA3AF);
        e.setBackground(bgStroke(c, WHITE, 10, 0xFFD1D5DB));
        e.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        return e;
    }

    /** w/h: -1 match, -2 wrap, >0 dp. margins in dp. */
    public static LinearLayout.LayoutParams lp(Context c, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w > 0 ? dp(c, w) : w, h > 0 ? dp(c, h) : h);
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    public static LinearLayout header(final Activity a, String title, boolean back) {
        LinearLayout h = new LinearLayout(a);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setBackgroundColor(PRIMARY);
        h.setPadding(dp(a, 12), dp(a, 14), dp(a, 12), dp(a, 14));
        if (back) {
            TextView b = tv(a, "→", 22, WHITE, true);
            b.setPadding(dp(a, 8), 0, dp(a, 12), 0);
            b.setOnClickListener(v -> a.finish());
            h.addView(b);
        }
        TextView t = tv(a, title, 19, WHITE, true);
        h.addView(t, new LinearLayout.LayoutParams(0, -2, 1f));
        return h;
    }

    public static String fmt(double p) {
        return new DecimalFormat("#,##0.##", java.text.DecimalFormatSymbols.getInstance(Locale.US)).format(p);
    }

    public static double num(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.trim().toCharArray()) {
            if (ch >= '٠' && ch <= '٩') sb.append((char) ('0' + (ch - '٠')));
            else if (ch == '٫') sb.append('.');
            else if (ch == ',' || ch == '،' || ch == ' ') continue;
            else sb.append(ch);
        }
        return Double.parseDouble(sb.toString());
    }

    public static void load(final ImageView iv, final String b64, final int sample) {
        if (b64 == null || b64.isEmpty()) { iv.setTag(null); iv.setImageDrawable(null); return; }
        final String key = b64.hashCode() + "_" + b64.length() + "_" + sample;
        iv.setTag(key);
        Bitmap cached = cache.get(key);
        if (cached != null) { iv.setImageBitmap(cached); return; }
        iv.setImageDrawable(null);
        EX.execute(() -> {
            try {
                byte[] d = Base64.decode(b64, Base64.DEFAULT);
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = sample;
                final Bitmap bm = BitmapFactory.decodeByteArray(d, 0, d.length, o);
                if (bm != null) {
                    cache.put(key, bm);
                    H.post(() -> { if (key.equals(iv.getTag())) iv.setImageBitmap(bm); });
                }
            } catch (Exception ignored) { }
        });
    }

    public static String encode(Context c, Uri uri) throws Exception {
        InputStream in = c.getContentResolver().openInputStream(uri);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, o);
        in.close();
        int max = Math.max(o.outWidth, o.outHeight);
        int s = 1;
        while (max / s > 1600) s *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = s;
        in = c.getContentResolver().openInputStream(uri);
        Bitmap b = BitmapFactory.decodeStream(in, null, o);
        in.close();
        if (b == null) throw new Exception("decode");
        int rot = 0;
        try {
            in = c.getContentResolver().openInputStream(uri);
            ExifInterface ex = new ExifInterface(in);
            int ori = ex.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (ori == 6) rot = 90; else if (ori == 3) rot = 180; else if (ori == 8) rot = 270;
            in.close();
        } catch (Exception ignored) { }
        float sc = Math.min(1f, 800f / Math.max(b.getWidth(), b.getHeight()));
        Matrix m = new Matrix();
        m.postScale(sc, sc);
        if (rot != 0) m.postRotate(rot);
        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 70, bo);
        return Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP);
    }
}
