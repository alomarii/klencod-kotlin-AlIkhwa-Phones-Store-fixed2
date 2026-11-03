package com.alikhwa.phones;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView count;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) { finish(); return; }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);
        LinearLayout head = U.header(this, "⚙️ لوحة الإدارة", true);
        TextView out = U.tv(this, "خروج", 14, U.WHITE, true);
        out.setOnClickListener(v -> { FirebaseAuth.getInstance().signOut(); finish(); });
        head.addView(out);
        root.addView(head, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(U.dp(this, 10), U.dp(this, 10), U.dp(this, 10), U.dp(this, 4));
        TextView add = U.btn(this, "+ منتج", U.ACCENT);
        add.setOnClickListener(v -> startActivity(new Intent(this, ProductEditActivity.class)));
        TextView cats = U.btn(this, "الأقسام", U.PRIMARY);
        cats.setOnClickListener(v -> showCats());
        TextView set = U.btn(this, "الإعدادات", U.PRIMARY);
        set.setOnClickListener(v -> showSettings());
        actions.addView(add, new LinearLayout.LayoutParams(0, -2, 1f));
        actions.addView(cats, U.lp(this, 0, -2, 6, 0, 0, 0));
        actions.addView(set, U.lp(this, 0, -2, 6, 0, 0, 0));
        ((LinearLayout.LayoutParams) cats.getLayoutParams()).weight = 1f;
        ((LinearLayout.LayoutParams) set.getLayoutParams()).weight = 1f;
        root.addView(actions, new LinearLayout.LayoutParams(-1, -2));

        count = U.tv(this, "", 13, U.GRAY, false);
        root.addView(count, U.lp(this, -2, -2, 16, 4, 16, 4));

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(U.dp(this, 10), U.dp(this, 4), U.dp(this, 10), U.dp(this, 16));
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list == null) return;
        count.setText("جاري التحميل...");
        Repo.loadAll(ok -> render());
    }

    private void render() {
        list.removeAllViews();
        count.setText("عدد المنتجات: " + Repo.products.size() + "  |  الأقسام: " + Repo.cats.size());
        for (final Product p : Repo.products) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(U.bg(this, U.WHITE, 12));
            row.setPadding(U.dp(this, 10), U.dp(this, 8), U.dp(this, 10), U.dp(this, 8));
            ImageView th = new ImageView(this);
            th.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            U.load(th, p.images.isEmpty() ? null : p.images.get(0), 4);
            row.addView(th, new LinearLayout.LayoutParams(U.dp(this, 56), U.dp(this, 56)));
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.addView(U.tv(this, p.name, 14, U.TEXT, true));
            info.addView(U.tv(this, U.fmt(p.price) + " " + Repo.currency + "  •  " + p.category
                    + (p.available ? "" : "  •  غير متوفر"), 12, U.GRAY, false));
            row.addView(info, U.lp(this, 0, -2, 10, 0, 0, 0));
            ((LinearLayout.LayoutParams) info.getLayoutParams()).weight = 1f;
            row.addView(U.tv(this, "✏️", 20, U.TEXT, false));
            row.setOnClickListener(v -> startActivity(
                    new Intent(this, ProductEditActivity.class).putExtra("id", p.id)));
            list.addView(row, U.lp(this, -1, -2, 0, 0, 0, 8));
        }
    }

    private void showCats() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(U.dp(this, 16), U.dp(this, 8), U.dp(this, 16), U.dp(this, 8));
        final EditText n = U.et(this, "اسم القسم الجديد (مثال: هواتف)");
        n.setSingleLine(true);
        box.addView(n);
        final AlertDialog[] dlg = new AlertDialog[1];
        TextView add = U.btn(this, "+ إضافة قسم", U.ACCENT);
        add.setOnClickListener(v -> {
            String s = n.getText().toString().trim();
            if (s.isEmpty()) return;
            Map<String, Object> m = new HashMap<>();
            m.put("name", s);
            m.put("order", System.currentTimeMillis());
            FirebaseFirestore.getInstance().collection("categories").add(m).addOnCompleteListener(t -> {
                if (!t.isSuccessful()) Toast.makeText(this, "فشلت الإضافة", Toast.LENGTH_LONG).show();
                dlg[0].dismiss();
                Repo.dirty = true;
                Repo.loadCats(ok -> { render(); showCats(); });
            });
        });
        box.addView(add, U.lp(this, -1, -2, 0, 8, 0, 8));
        for (final Repo.Cat c : Repo.cats) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, U.dp(this, 8), 0, U.dp(this, 8));
            TextView name = U.tv(this, c.name, 16, U.TEXT, false);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView del = U.tv(this, "حذف", 14, U.RED, true);
            del.setPadding(U.dp(this, 12), U.dp(this, 6), U.dp(this, 12), U.dp(this, 6));
            del.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setMessage("حذف القسم \"" + c.name + "\"؟ (المنتجات داخله لن تُحذف)")
                    .setPositiveButton("حذف", (d, w) -> {
                        dlg[0].dismiss();
                        FirebaseFirestore.getInstance().collection("categories").document(c.id).delete()
                                .addOnCompleteListener(t -> {
                                    Repo.dirty = true;
                                    Repo.loadCats(ok -> { render(); showCats(); });
                                });
                    })
                    .setNegativeButton("إلغاء", null).show());
            row.addView(del);
            box.addView(row);
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        dlg[0] = new AlertDialog.Builder(this).setTitle("إدارة الأقسام").setView(sv)
                .setNegativeButton("إغلاق", null).create();
        dlg[0].show();
    }

    private void showSettings() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(U.dp(this, 16), U.dp(this, 8), U.dp(this, 16), U.dp(this, 8));
        final EditText sn = U.et(this, "اسم المتجر");
        sn.setText(Repo.storeName);
        final EditText wa = U.et(this, "رقم واتساب (مع رمز الدولة)");
        wa.setText(Repo.whatsapp);
        wa.setInputType(InputType.TYPE_CLASS_PHONE);
        final EditText cu = U.et(this, "العملة");
        cu.setText(Repo.currency);
        final EditText pw = U.et(this, "كلمة سر جديدة (اتركها فارغة بدون تغيير)");
        pw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(sn);
        box.addView(wa, U.lp(this, -1, -2, 0, 8, 0, 0));
        box.addView(cu, U.lp(this, -1, -2, 0, 8, 0, 0));
        box.addView(pw, U.lp(this, -1, -2, 0, 8, 0, 0));
        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        new AlertDialog.Builder(this).setTitle("الإعدادات").setView(sv)
                .setPositiveButton("حفظ", (d, w) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("storeName", sn.getText().toString().trim());
                    m.put("whatsapp", wa.getText().toString().trim());
                    m.put("currency", cu.getText().toString().trim());
                    FirebaseFirestore.getInstance().collection("settings").document("store")
                            .set(m, SetOptions.merge()).addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    Repo.storeName = sn.getText().toString().trim().isEmpty() ? Repo.storeName : sn.getText().toString().trim();
                                    Repo.whatsapp = wa.getText().toString().trim().isEmpty() ? Repo.whatsapp : wa.getText().toString().trim();
                                    Repo.currency = cu.getText().toString().trim().isEmpty() ? Repo.currency : cu.getText().toString().trim();
                                    Toast.makeText(this, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_LONG).show();
                                }
                            });
                    String np = pw.getText().toString();
                    FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                    if (!np.isEmpty() && u != null) {
                        if (np.length() < 6) {
                            Toast.makeText(this, "كلمة السر يجب ألا تقل عن 6 أحرف", Toast.LENGTH_LONG).show();
                        } else {
                            u.updatePassword(np).addOnCompleteListener(t -> Toast.makeText(this,
                                    t.isSuccessful() ? "تم تغيير كلمة السر" : "تعذر تغيير كلمة السر، سجّل الدخول من جديد وأعد المحاولة",
                                    Toast.LENGTH_LONG).show());
                        }
                    }
                })
                .setNegativeButton("إلغاء", null).show();
    }
}
