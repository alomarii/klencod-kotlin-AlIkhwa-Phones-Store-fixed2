package com.alikhwa.phones;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {
    static final String EMAIL = "alomari@alikhwa.store";
    private EditText user, pass;
    private TextView btn;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        U.rtl(this);
        if (auth.getCurrentUser() != null && EMAIL.equals(auth.getCurrentUser().getEmail())) {
            go();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.BG);
        root.addView(U.header(this, "تسجيل دخول الإدارة", true), new LinearLayout.LayoutParams(-1, -2));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(U.dp(this, 24), U.dp(this, 40), U.dp(this, 24), U.dp(this, 24));
        TextView ic = U.tv(this, "🔐", 56, U.TEXT, false);
        ic.setGravity(Gravity.CENTER);
        box.addView(ic);
        user = U.et(this, "اسم المستخدم");
        user.setSingleLine(true);
        box.addView(user, U.lp(this, -1, -2, 0, 20, 0, 0));
        pass = U.et(this, "كلمة السر");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(pass, U.lp(this, -1, -2, 0, 10, 0, 0));
        btn = U.btn(this, "دخول", U.PRIMARY);
        btn.setOnClickListener(v -> login());
        box.addView(btn, U.lp(this, -1, -2, 0, 18, 0, 0));
        root.addView(box, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void login() {
        final String u = user.getText().toString().trim().toLowerCase();
        final String p = pass.getText().toString();
        if (!u.equals("alomari") || p.length() < 6) { bad(null); return; }
        btn.setText("جاري الدخول...");
        btn.setClickable(false);
        auth.signInWithEmailAndPassword(EMAIL, p).addOnCompleteListener(t -> {
            if (t.isSuccessful()) { go(); return; }
            if (p.equals("alomari")) {
                // أول تشغيل: إنشاء حساب الإدارة تلقائياً
                auth.createUserWithEmailAndPassword(EMAIL, p).addOnCompleteListener(t2 -> {
                    if (t2.isSuccessful()) go(); else bad(t2.getException());
                });
            } else {
                bad(t.getException());
            }
        });
    }

    private void bad(Exception e) {
        btn.setText("دخول");
        btn.setClickable(true);
        String m = "اسم المستخدم أو كلمة السر غير صحيحة";
        if (e != null && e.getMessage() != null && e.getMessage().toLowerCase().contains("network"))
            m = "تحقق من الاتصال بالإنترنت";
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }

    private void go() {
        startActivity(new Intent(this, AdminActivity.class));
        finish();
    }
}
