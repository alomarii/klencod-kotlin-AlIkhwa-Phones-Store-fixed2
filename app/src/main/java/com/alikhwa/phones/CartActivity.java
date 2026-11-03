package com.alikhwa.phones;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView total;
    private EditText nameEt, noteEt;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);
        LinearLayout head = U.header(this, "🛒 سلة المشتريات", true);
        TextView clear = U.tv(this, "تفريغ", 14, U.WHITE, true);
        clear.setOnClickListener(v -> { Cart.items.clear(); refresh(); });
        head.addView(clear);
        root.addView(head, new LinearLayout.LayoutParams(-1, -2));

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(U.dp(this, 12), U.dp(this, 12), U.dp(this, 12), U.dp(this, 12));
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setBackgroundColor(U.WHITE);
        bottom.setElevation(U.dp(this, 8));
        bottom.setPadding(U.dp(this, 14), U.dp(this, 10), U.dp(this, 14), U.dp(this, 12));
        total = U.tv(this, "", 18, U.TEXT, true);
        bottom.addView(total);
        nameEt = U.et(this, "اسمك (اختياري)");
        nameEt.setSingleLine(true);
        bottom.addView(nameEt, U.lp(this, -1, -2, 0, 8, 0, 0));
        noteEt = U.et(this, "ملاحظات / العنوان (اختياري)");
        bottom.addView(noteEt, U.lp(this, -1, -2, 0, 6, 0, 0));
        TextView send = U.btn(this, "إرسال الطلب عبر واتساب", 0xFF25D366);
        send.setOnClickListener(v -> sendOrder());
        bottom.addView(send, U.lp(this, -1, -2, 0, 10, 0, 0));
        root.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    @Override
    protected void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        list.removeAllViews();
        if (Cart.items.isEmpty()) {
            TextView e = U.tv(this, "السلة فارغة", 17, U.GRAY, false);
            e.setGravity(Gravity.CENTER);
            e.setPadding(0, U.dp(this, 60), 0, 0);
            list.addView(e, new LinearLayout.LayoutParams(-1, -2));
        }
        for (final Cart.Item it : new ArrayList<>(Cart.items.values())) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(U.bg(this, U.WHITE, 14));
            row.setPadding(U.dp(this, 10), U.dp(this, 10), U.dp(this, 10), U.dp(this, 10));
            ImageView th = new ImageView(this);
            th.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            U.load(th, it.p.images.isEmpty() ? null : it.p.images.get(0), 4);
            row.addView(th, new LinearLayout.LayoutParams(U.dp(this, 70), U.dp(this, 70)));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(U.tv(this, it.p.name, 14, U.TEXT, true));
            info.addView(U.tv(this, U.fmt(it.p.price) + " " + Repo.currency, 14, U.ACCENT, true));
            LinearLayout q = new LinearLayout(this);
            q.setOrientation(LinearLayout.HORIZONTAL);
            q.setGravity(Gravity.CENTER_VERTICAL);
            TextView minus = U.tv(this, "−", 20, U.WHITE, true);
            minus.setGravity(Gravity.CENTER);
            minus.setBackground(U.bg(this, U.PRIMARY, 8));
            TextView plus = U.tv(this, "+", 20, U.WHITE, true);
            plus.setGravity(Gravity.CENTER);
            plus.setBackground(U.bg(this, U.PRIMARY, 8));
            q.addView(minus, new LinearLayout.LayoutParams(U.dp(this, 34), U.dp(this, 34)));
            TextView qty = U.tv(this, String.valueOf(it.qty), 16, U.TEXT, true);
            qty.setGravity(Gravity.CENTER);
            q.addView(qty, new LinearLayout.LayoutParams(U.dp(this, 40), -2));
            q.addView(plus, new LinearLayout.LayoutParams(U.dp(this, 34), U.dp(this, 34)));
            info.addView(q, U.lp(this, -2, -2, 0, 6, 0, 0));
            minus.setOnClickListener(v -> { if (it.qty > 1) it.qty--; refresh(); });
            plus.setOnClickListener(v -> { it.qty++; refresh(); });
            row.addView(info, U.lp(this, 0, -2, 10, 0, 6, 0));
            ((LinearLayout.LayoutParams) info.getLayoutParams()).weight = 1f;

            TextView del = U.tv(this, "🗑", 22, U.RED, false);
            del.setPadding(U.dp(this, 8), U.dp(this, 8), U.dp(this, 8), U.dp(this, 8));
            del.setOnClickListener(v -> { Cart.items.remove(it.p.id); refresh(); });
            row.addView(del);
            list.addView(row, U.lp(this, -1, -2, 0, 0, 0, 8));
        }
        total.setText("الإجمالي: " + U.fmt(Cart.total()) + " " + Repo.currency);
    }

    private void sendOrder() {
        if (Cart.items.isEmpty()) {
            Toast.makeText(this, "السلة فارغة", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 طلب جديد من تطبيق ").append(Repo.storeName).append("\n\n");
        int i = 1;
        for (Cart.Item it : Cart.items.values()) {
            sb.append(i++).append(") ").append(it.p.name).append("\n   ")
              .append(it.qty).append(" × ").append(U.fmt(it.p.price)).append(" = ")
              .append(U.fmt(it.qty * it.p.price)).append(" ").append(Repo.currency).append("\n");
        }
        sb.append("\nالإجمالي: ").append(U.fmt(Cart.total())).append(" ").append(Repo.currency);
        String n = nameEt.getText().toString().trim();
        String note = noteEt.getText().toString().trim();
        if (!n.isEmpty()) sb.append("\nالاسم: ").append(n);
        if (!note.isEmpty()) sb.append("\nملاحظات: ").append(note);

        String num = Repo.whatsapp.replaceAll("[^0-9]", "");
        if (num.startsWith("00")) num = num.substring(2);
        Uri u = Uri.parse("https://wa.me/" + num + "?text=" + Uri.encode(sb.toString()));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, u));
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح واتساب", Toast.LENGTH_LONG).show();
        }
    }
}
