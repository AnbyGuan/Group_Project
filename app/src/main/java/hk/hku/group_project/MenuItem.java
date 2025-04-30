package hk.hku.group_project;

import java.util.List;

public class MenuItem {
    public String name;
    public List<String> ingredients;  // 原料名称列表
    public String steps;              // 步骤文字
    public boolean isReady;              //是否有全部材料
    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }



    public MenuItem() {}

    public MenuItem(String name, List<String> ingredients, String steps) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public MenuItem(List<String> ingredients, String steps, boolean isReady, String name) {
        this.ingredients = ingredients;
        this.steps = steps;
        this.isReady = isReady;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "name='" + name + '\'' +
                ", ingredients=" + ingredients +
                ", steps='" + steps + '\'' +
                ", isReady=" + isReady +
                '}';
    }
}
