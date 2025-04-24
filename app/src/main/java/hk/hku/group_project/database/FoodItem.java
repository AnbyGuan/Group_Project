package hk.hku.group_project.database;

import java.util.List;

public class FoodItem {
    public String name;
    public List<String> owners;  // 每个食材的归属人（uid 列表）
    public String expiry;        // 保质期（例如 2025-12-01）
    public String storage;       // 冷藏 / 冷冻
    public double price;

    public FoodItem() {} // Firebase 需要空构造函数

    public FoodItem(String name, List<String> owners, String expiry, String storage, double price) {
        this.name = name;
        this.owners = owners;
        this.expiry = expiry;
        this.storage = storage;
        this.price = price;
    }
}
