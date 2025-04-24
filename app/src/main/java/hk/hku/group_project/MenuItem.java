package hk.hku.group_project;

import java.util.List;

public class MenuItem {
    public String name;
    public List<String> ingredients;  // 原料名称列表
    public String steps;              // 步骤文字

    public MenuItem() {}

    public MenuItem(String name, List<String> ingredients, String steps) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
    }
}
