package com.alikhwa.phones;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout chips;
    private EditText search;
    private TextView cartBtn, empty, title;
    private ProgressBar bar;
    private String selCat = null;
    private final List<Product> shown = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setBackgroundColor(U.PRIMARY);
        head.setPadding(U.dp(this, 16), U.dp(this, 14), U.dp(this, 16), U.dp(this, 14));
        title = U.tv(this, "📱 " + Repo.storeName, 20, U.WHITE, true);
        // ضغطة مطولة على اسم المتجر تفتح تسجيل دخول الإدارة
        title.setOnLongClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
            return true;
        });
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        cartBtn = U.tv(this, "🛒 0", 16, U.WHITE, true);
        cartBtn.setPadding(U.dp(this, 14), U.dp(this, 8), U.dp(this, 14), U.dp(this, 8));
        cartBtn.setBackground(U.bg(this, 0x33FFFFFF, 20));
        cartBtn.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        head.addView(cartBtn);
        root.addView(head, new LinearLayout.LayoutParams(-1, -2));

        search = U.et(this, "🔍 ابحث عن منتج...");
        search.setSingleLine(true);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b2, int c) { }
            public void onTextChanged(CharSequence s, int a, int b2, int c) { applyFilter(); }
            public void afterTextChanged(Editable s) { }
        });
        root.addView(search, U.lp(this, -1, -2, 12, 10, 12, 4));

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(U.dp(this, 8), U.dp(this, 4), U.dp(this, 8), U.dp(this, 4));
        hs.addView(chips);
        root.addView(hs, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout holder = new FrameLayout(this);
        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setClipToPadding(false);
        int pd = U.dp(this, 6);
        rv.setPadding(pd, pd, pd, pd);
        adapter = new Adapter();
        rv.setAdapter(adapter);
        holder.addView(rv, new FrameLayout.LayoutParams(-1, -1));
        empty = U.tv(this, "لا توجد منتجات", 16, U.GRAY, false);
        empty.setGravity(Gravity.CENTER);
        empty.setVisibility(android.view.View.GONE);
        holder.addView(empty, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        bar = new ProgressBar(this);
        holder.addView(bar, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        root.addView(holder, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartBtn.setText("🛒 " + Cart.count());
        boolean need = !Repo.loaded || Repo.dirty || System.currentTimeMillis() - Repo.lastLoad > 300000;
        if (need) {
            bar.setVisibility(android.view.View.VISIBLE);
            Repo.loadAll(ok -> {
                bar.setVisibility(android.view.View.GONE);
                if (!ok && Repo.products.isEmpty()) {
                    empty.setText("تعذر تحميل المنتجات\nتأكد من الاتصال بالإنترنت");
                }
                render();
            });
        } else {
            render();
        }
    }

    private void render() {
        title.setText("📱 " + Repo.storeName);
        buildChips();
        applyFilter();
    }

    private void buildChips() {
        chips.removeAllViews();
        addChip("الكل", null);
        for (Repo.Cat c : Repo.cats) addChip(c.name, c.name);
    }

    private void addChip(String label, final String value) {
        boolean sel = TextUtils.equals(selCat, value);
        TextView t = U.tv(this, label, 14, sel ? U.WHITE : U.TEXT, true);
        t.setPadding(U.dp(this, 16), U.dp(this, 8), U.dp(this, 16), U.dp(this, 8));
        t.setBackground(sel ? U.bg(this, U.ACCENT, 20) : U.bgStroke(this, U.WHITE, 20, 0xFFD1D5DB));
        t.setOnClickListener(v -> { selCat = value; buildChips(); applyFilter(); });
        chips.addView(t, U.lp(this, -2, -2, 4, 0, 4, 0));
    }

    private void applyFilter() {
        String q = search.getText().toString().trim().toLowerCase();
        shown.clear();
        for (Product p : Repo.products) {
            if (selCat != null && !selCat.equals(p.category)) continue;
            if (!q.isEmpty() && !p.name.toLowerCase().contains(q)) continue;
            shown.add(p);
        }
        adapter.notifyDataSetChanged();
        if (shown.isEmpty() && bar.getVisibility() != android.view.View.VISIBLE) {
            if (Repo.products.isEmpty() && !Repo.loaded) { /* message set above */ } else empty.setText("لا توجد منتجات");
            empty.setVisibility(android.view.View.VISIBLE);
        } else {
            empty.setVisibility(android.view.View.GONE);
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView name, price, tag;
            VH(android.view.View v) { super(v); }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int type) {
            Context c = parent.getContext();
            LinearLayout card = new LinearLayout(c);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(U.bg(c, U.WHITE, 14));
            card.setElevation(U.dp(c, 2));
            RecyclerView.LayoutParams rl = new RecyclerView.LayoutParams(-1, -2);
            int m = U.dp(c, 6);
            rl.setMargins(m, m, m, m);
            card.setLayoutParams(rl);

            FrameLayout fl = new FrameLayout(c);
            ImageView img = new ImageView(c);
            img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            img.setPadding(m, m, m, m);
            fl.addView(img, new FrameLayout.LayoutParams(-1, U.dp(c, 150)));
            TextView tag = U.tv(c, "غير متوفر", 11, U.WHITE, true);
            tag.setBackground(U.bg(c, U.RED, 8));
            tag.setPadding(U.dp(c, 8), U.dp(c, 3), U.dp(c, 8), U.dp(c, 3));
            FrameLayout.LayoutParams tl = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
            tl.setMargins(m, m, m, m);
            fl.addView(tag, tl);
            card.addView(fl, new LinearLayout.LayoutParams(-1, -2));

            TextView name = U.tv(c, "", 14, U.TEXT, true);
            name.setLines(2);
            name.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(name, U.lp(c, -1, -2, 10, 4, 10, 0));
            TextView price = U.tv(c, "", 15, U.ACCENT, true);
            card.addView(price, U.lp(c, -1, -2, 10, 2, 10, 10));

            VH vh = new VH(card);
            vh.img = img; vh.name = name; vh.price = price; vh.tag = tag;
            return vh;
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            final Product p = shown.get(pos);
            h.name.setText(p.name);
            h.price.setText(U.fmt(p.price) + " " + Repo.currency);
            h.tag.setVisibility(p.available ? android.view.View.GONE : android.view.View.VISIBLE);
            U.load(h.img, p.images.isEmpty() ? null : p.images.get(0), 2);
            h.itemView.setOnClickListener(v -> startActivity(
                    new Intent(MainActivity.this, ProductDetailActivity.class).putExtra("id", p.id)));
        }

        @Override
        public int getItemCount() { return shown.size(); }
    }
}
