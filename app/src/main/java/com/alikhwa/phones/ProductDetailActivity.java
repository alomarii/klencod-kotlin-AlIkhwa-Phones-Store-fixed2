package com.alikhwa.phones;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ProductDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        final Product p = Repo.find(getIntent().getStringExtra("id"));
        if (p == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);
        root.addView(U.header(this, "تفاصيل المنتج", true), new LinearLayout.LayoutParams(-1, -2));

        ScrollView sv = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        sv.addView(col);

        FrameLayout pf = new FrameLayout(this);
        pf.setBackgroundColor(U.WHITE);
        ViewPager pager = new ViewPager(this);
        pager.setAdapter(new PagerAdapter() {
            @Override public int getCount() { return Math.max(1, p.images.size()); }
            @Override public boolean isViewFromObject(View v, Object o) { return v == o; }
            @Override public Object instantiateItem(ViewGroup c, int pos) {
                ImageView iv = new ImageView(c.getContext());
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                iv.setBackgroundColor(U.WHITE);
                U.load(iv, p.images.isEmpty() ? null : p.images.get(pos), 1);
                c.addView(iv);
                return iv;
            }
            @Override public void destroyItem(ViewGroup c, int pos, Object o) { c.removeView((View) o); }
        });
        pf.addView(pager, new FrameLayout.LayoutParams(-1, U.dp(this, 320)));
        final TextView counter = U.tv(this, "1/" + Math.max(1, p.images.size()), 12, U.WHITE, true);
        counter.setBackground(U.bg(this, 0x99000000, 12));
        counter.setPadding(U.dp(this, 10), U.dp(this, 4), U.dp(this, 10), U.dp(this, 4));
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cl.bottomMargin = U.dp(this, 10);
        pf.addView(counter, cl);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int pos) { counter.setText((pos + 1) + "/" + Math.max(1, p.images.size())); }
        });
        col.addView(pf, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackground(U.bg(this, U.WHITE, 16));
        info.setPadding(U.dp(this, 16), U.dp(this, 16), U.dp(this, 16), U.dp(this, 16));
        col.addView(info, U.lp(this, -1, -2, 12, 12, 12, 12));

        info.addView(U.tv(this, p.name, 22, U.TEXT, true));
        info.addView(U.tv(this, U.fmt(p.price) + " " + Repo.currency, 22, U.ACCENT, true), U.lp(this, -2, -2, 0, 6, 0, 0));
        LinearLayout tags = new LinearLayout(this);
        tags.setOrientation(LinearLayout.HORIZONTAL);
        TextView cat = U.tv(this, p.category, 13, U.PRIMARY, true);
        cat.setBackground(U.bg(this, 0xFFE3ECFB, 14));
        cat.setPadding(U.dp(this, 12), U.dp(this, 4), U.dp(this, 12), U.dp(this, 4));
        tags.addView(cat);
        TextView av = U.tv(this, p.available ? "متوفر" : "غير متوفر", 13, U.WHITE, true);
        av.setBackground(U.bg(this, p.available ? U.GREEN : U.RED, 14));
        av.setPadding(U.dp(this, 12), U.dp(this, 4), U.dp(this, 12), U.dp(this, 4));
        tags.addView(av, U.lp(this, -2, -2, 8, 0, 0, 0));
        info.addView(tags, U.lp(this, -2, -2, 0, 10, 0, 0));
        if (!p.description.isEmpty()) {
            info.addView(U.tv(this, "التفاصيل", 16, U.TEXT, true), U.lp(this, -2, -2, 0, 16, 0, 4));
            TextView d = U.tv(this, p.description, 15, 0xFF374151, false);
            d.setLineSpacing(0, 1.25f);
            info.addView(d);
        }
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(U.WHITE);
        bar.setElevation(U.dp(this, 8));
        bar.setPadding(U.dp(this, 12), U.dp(this, 10), U.dp(this, 12), U.dp(this, 10));
        TextView add = U.btn(this, "🛒 أضف إلى السلة", p.available ? U.ACCENT : 0xFF9CA3AF);
        add.setOnClickListener(v -> {
            if (!p.available) { Toast.makeText(this, "هذا المنتج غير متوفر حالياً", Toast.LENGTH_SHORT).show(); return; }
            Cart.add(p);
            Toast.makeText(this, "تمت الإضافة إلى السلة", Toast.LENGTH_SHORT).show();
        });
        bar.addView(add, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView go = U.btn(this, "السلة", U.PRIMARY);
        go.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        bar.addView(go, U.lp(this, -2, -2, 8, 0, 0, 0));
        root.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
}
