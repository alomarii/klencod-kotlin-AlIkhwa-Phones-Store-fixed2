package com.alikhwa.phones;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class Product {
    public String id, name, category, description;
    public double price;
    public boolean available = true;
    public long createdAt;
    public List<String> images = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static Product from(DocumentSnapshot d) {
        Product p = new Product();
        p.id = d.getId();
        p.name = d.getString("name") == null ? "" : d.getString("name");
        p.category = d.getString("category") == null ? "" : d.getString("category");
        p.description = d.getString("description") == null ? "" : d.getString("description");
        Double pr = d.getDouble("price");
        p.price = pr == null ? 0 : pr;
        Boolean av = d.getBoolean("available");
        p.available = av == null || av;
        Long ct = d.getLong("createdAt");
        p.createdAt = ct == null ? 0 : ct;
        List<String> im = (List<String>) d.get("images");
        if (im != null) p.images = new ArrayList<>(im);
        return p;
    }
}
