package com.alikhwa.phones;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductEditActivity extends AppCompatActivity {
    static final int MAX = 4, PICK = 11;
    private String id;
    private Product cur;
    private final List<String> imgs = new ArrayList<>();
    private final List<String> catNames = new ArrayList<>();
    private EditText name, price, desc;
    private Spinner spin;
    private CheckBox avail;
    private LinearLayout strip;
    private TextView save;
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        id = getIntent().getStringExtra("id");
        if (id != null) cur = Repo.find(id);
        for (Repo.Cat c : Repo.cats) catNames.add(c.name);
        if (catNames.isEmpty()) {
            Toast.makeText(this, "أضف قسماً أولاً من زر (الأقسام)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);
        root.addView(U.header(this, cur == null ? "إضافة منتج" : "تعديل منتج", true), new LinearLayout.LayoutParams(-1, -2));
        ScrollView sv = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(U.dp(this, 16), U.dp(this, 14), U.dp(this, 16), U.dp(this, 24));
        sv.addView(col);

        col.addView(U.tv(this, "اسم المنتج", 14, U.TEXT, true));
        name = U.et(this, "مثال: Samsung Galaxy A54");
        col.addView(name, U.lp(this, -1, -2, 0, 4, 0, 10));
        col.addView(U.tv(this, "السعر", 14, U.TEXT, true));
        price = U.et(this, "0");
        price.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        col.addView(price, U.lp(this, -1, -2, 0, 4, 0, 10));
        col.addView(U.tv(this, "القسم", 14, U.TEXT, true));
        spin = new Spinner(this);
        spin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catNames));
        spin.setBackground(U.bgStroke(this, U.WHITE, 10, 0xFFD1D5DB));
        col.addView(spin, U.lp(this, -1, 48, 0, 4, 0, 10));
        col.addView(U.tv(this, "التفاصيل", 14, U.TEXT, true));
        desc = U.et(this, "المواصفات: الشاشة، الذاكرة، الكاميرا ...");
        desc.setMinLines(4);
        desc.setGravity(Gravity.TOP | Gravity.START);
        desc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        col.addView(desc, U.lp(this, -1, -2, 0, 4, 0, 10));
        avail = new CheckBox(this);
        avail.setText("متوفر");
        avail.setChecked(true);
        avail.setTextSize(15);
        col.addView(avail);

        col.addView(U.tv(this, "الصور (حتى " + MAX + ")", 14, U.TEXT, true), U.lp(this, -2, -2, 0, 8, 0, 4));
        HorizontalScrollView hs = new HorizontalScrollView(this);
        strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        hs.addView(strip);
        col.addView(hs, new LinearLayout.LayoutParams(-1, -2));

        save = U.btn(this, "حفظ", U.ACCENT);
        save.setOnClickListener(v -> save());
        col.addView(save, U.lp(this, -1, -2, 0, 20, 0, 0));
        if (cur != null) {
            TextView del = U.btn(this, "حذف المنتج", U.RED);
            del.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setMessage("هل تريد حذف هذا المنتج نهائياً؟")
                    .setPositiveButton("حذف", (d, w) -> Repo.db().collection("products").document(id).delete()
                            .addOnCompleteListener(t -> { Repo.dirty = true; finish(); }))
                    .setNegativeButton("إلغاء", null).show());
            col.addView(del, U.lp(this, -1, -2, 0, 10, 0, 0));
            name.setText(cur.name);
            price.setText(U.fmt(cur.price).replace(",", ""));
            desc.setText(cur.description);
            avail.setChecked(cur.available);
            int ix = catNames.indexOf(cur.category);
            if (ix >= 0) spin.setSelection(ix);
            imgs.addAll(cur.images);
        }
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        renderStrip();
    }

    private void renderStrip() {
        strip.removeAllViews();
        for (int i = 0; i < imgs.size(); i++) {
            final int idx = i;
            FrameLayout f = new FrameLayout(this);
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackground(U.bg(this, U.WHITE, 10));
            U.load(iv, imgs.get(i), 2);
            f.addView(iv, new FrameLayout.LayoutParams(U.dp(this, 90), U.dp(this, 90)));
            TextView x = U.tv(this, "✕", 14, U.WHITE, true);
            x.setGravity(Gravity.CENTER);
            x.setBackground(U.bg(this, U.RED, 12));
            x.setOnClickListener(v -> { imgs.remove(idx); renderStrip(); });
            f.addView(x, new FrameLayout.LayoutParams(U.dp(this, 26), U.dp(this, 26), Gravity.TOP | Gravity.END));
            strip.addView(f, U.lp(this, -2, -2, 0, 0, 8, 0));
        }
        if (imgs.size() < MAX) {
            TextView addImg = U.tv(this, "+\nصورة", 14, U.PRIMARY, true);
            addImg.setGravity(Gravity.CENTER);
            addImg.setBackground(U.bgStroke(this, U.WHITE, 10, U.PRIMARY));
            addImg.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(i, "اختر الصور"), PICK);
            });
            strip.addView(addImg, new LinearLayout.LayoutParams(U.dp(this, 90), U.dp(this, 90)));
        }
    }

    @Override
    protected void onActivityResult(int rc, int res, Intent d) {
        super.onActivityResult(rc, res, d);
        if (rc != PICK || res != RESULT_OK || d == null) return;
        final List<Uri> uris = new ArrayList<>();
        if (d.getClipData() != null) {
            for (int i = 0; i < d.getClipData().getItemCount(); i++) uris.add(d.getClipData().getItemAt(i).getUri());
        } else if (d.getData() != null) {
            uris.add(d.getData());
        }
        final int room = MAX - imgs.size();
        Toast.makeText(this, "جاري معالجة الصور...", Toast.LENGTH_SHORT).show();
        U.EX.execute(() -> {
            final List<String> out = new ArrayList<>();
            for (Uri u : uris) {
                if (out.size() >= room) break;
                try { out.add(U.encode(this, u)); } catch (Exception ignored) { }
            }
            U.H.post(() -> { imgs.addAll(out); renderStrip(); });
        });
    }

    private void save() {
        if (busy) return;
        String n = name.getText().toString().trim();
        if (n.isEmpty()) { Toast.makeText(this, "اكتب اسم المنتج", Toast.LENGTH_SHORT).show(); return; }
        double pr;
        try { pr = U.num(price.getText().toString()); }
        catch (Exception e) { Toast.makeText(this, "السعر غير صحيح", Toast.LENGTH_SHORT).show(); return; }
        Map<String, Object> m = new HashMap<>();
        m.put("name", n);
        m.put("price", pr);
        m.put("category", catNames.get(spin.getSelectedItemPosition()));
        m.put("description", desc.getText().toString().trim());
        m.put("available", avail.isChecked());
        m.put("images", new ArrayList<>(imgs));
        m.put("createdAt", cur != null ? cur.createdAt : System.currentTimeMillis());
        busy = true;
        save.setText("جاري الحفظ...");
        DocumentReference ref = id == null ? Repo.db().collection("products").document()
                : Repo.db().collection("products").document(id);
        ref.set(m).addOnCompleteListener(t -> {
            busy = false;
            if (t.isSuccessful()) {
                Repo.dirty = true;
                Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                save.setText("حفظ");
                Toast.makeText(this, "فشل الحفظ (تأكد من الإنترنت وصلاحيات Firebase، أو قلّل عدد الصور)", Toast.LENGTH_LONG).show();
            }
        });
    }
}
