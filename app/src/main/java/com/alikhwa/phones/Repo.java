package com.alikhwa.phones;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Repo {
    public static class Cat {
        public String id, name;
        public long order;
    }

    public interface Done { void ok(boolean success); }

    public static final List<Product> products = new ArrayList<>();
    public static final List<Cat> cats = new ArrayList<>();
    public static String storeName = "الاخوة للهواتف";
    public static String whatsapp = "00967771921031";
    public static String currency = "ريال";
    public static boolean loaded = false, dirty = false;
    public static long lastLoad = 0;

    public static FirebaseFirestore db() { return FirebaseFirestore.getInstance(); }

    public static void loadAll(final Done d) {
        db().collection("settings").document("store").get().addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                DocumentSnapshot s = t.getResult();
                if (s.getString("storeName") != null && !s.getString("storeName").isEmpty()) storeName = s.getString("storeName");
                if (s.getString("whatsapp") != null && !s.getString("whatsapp").isEmpty()) whatsapp = s.getString("whatsapp");
                if (s.getString("currency") != null && !s.getString("currency").isEmpty()) currency = s.getString("currency");
            }
            loadCats(okc -> {
                db().collection("products").get().addOnCompleteListener(t3 -> {
                    if (t3.isSuccessful() && t3.getResult() != null) {
                        products.clear();
                        for (DocumentSnapshot x : t3.getResult()) products.add(Product.from(x));
                        Collections.sort(products, (a, b) -> Long.compare(b.createdAt, a.createdAt));
                        loaded = true;
                        dirty = false;
                        lastLoad = System.currentTimeMillis();
                        d.ok(true);
                    } else {
                        d.ok(false);
                    }
                });
            });
        });
    }

    public static void loadCats(final Done d) {
        db().collection("categories").get().addOnCompleteListener(t2 -> {
            if (t2.isSuccessful() && t2.getResult() != null) {
                cats.clear();
                for (DocumentSnapshot x : t2.getResult()) {
                    Cat c = new Cat();
                    c.id = x.getId();
                    c.name = x.getString("name") == null ? "" : x.getString("name");
                    Long o = x.getLong("order");
                    c.order = o == null ? 0 : o;
                    cats.add(c);
                }
                Collections.sort(cats, (a, b) -> Long.compare(a.order, b.order));
                d.ok(true);
            } else {
                d.ok(false);
            }
        });
    }

    public static Product find(String id) {
        for (Product p : products) if (p.id.equals(id)) return p;
        return null;
    }
}
