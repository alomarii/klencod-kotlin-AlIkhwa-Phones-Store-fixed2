package com.alikhwa.phones;

import java.util.LinkedHashMap;

public class Cart {
    public static class Item {
        public Product p;
        public int qty;
    }

    public static final LinkedHashMap<String, Item> items = new LinkedHashMap<>();

    public static void add(Product p) {
        Item it = items.get(p.id);
        if (it == null) {
            it = new Item();
            it.p = p;
            it.qty = 0;
            items.put(p.id, it);
        }
        it.qty++;
    }

    public static int count() {
        int c = 0;
        for (Item i : items.values()) c += i.qty;
        return c;
    }

    public static double total() {
        double t = 0;
        for (Item i : items.values()) t += i.qty * i.p.price;
        return t;
    }
}
