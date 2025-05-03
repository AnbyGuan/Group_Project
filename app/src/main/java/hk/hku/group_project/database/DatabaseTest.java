package hk.hku.group_project.database;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import hk.hku.group_project.MenuItem;

public class DatabaseTest {

    public static void testAddFood() {
        List<String> owners = Arrays.asList("uid1", "uid2");
        FoodItem food = new FoodItem("黄瓜", owners, "2025-12-01", "冷藏", 8.88);
        DatabaseHelper.addFood("group123", food, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Log.d("TestAddFood", "✅ 添加成功");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestAddFood", "❌ 添加失败：" + e.getMessage());
            }
        });
    }

    public static void testGetAllFoods() {
        DatabaseHelper.getAllFoodsWithId("group123", new FirebaseCallback<Map<String, FoodItem>>() {
            @Override
            public void onSuccess(Map<String, FoodItem> data) {
                for (Map.Entry<String, FoodItem> entry : data.entrySet()) {
                    String foodId = entry.getKey();
                    FoodItem food = entry.getValue();
                    Log.d("TestFood", "📦 ID=" + foodId + "，食材=" + food.name + "，拥有者=" + food.owners);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestFood", "❌ 获取失败：" + e.getMessage());
            }
        });
    }

    public static void testGetAllFoodsWithIdToUI(TestActivity activity) {
        DatabaseHelper.getAllFoodsWithId("group123", new FirebaseCallback<Map<String, FoodItem>>() {
            @Override
            public void onSuccess(Map<String, FoodItem> data) {
                activity.showResult("📦 当前食材列表（含 foodId）：");
                for (Map.Entry<String, FoodItem> entry : data.entrySet()) {
                    String foodId = entry.getKey();
                    FoodItem food = entry.getValue();
                    activity.showResult("▪️ " + foodId + " → " + food.name + "（" + food.owners + "）");
                }
            }

            @Override
            public void onFailure(Exception e) {
                activity.showResult("❌ 获取失败：" + e.getMessage());
            }
        });
    }


    public static void testDeleteFood(String foodId) {
        DatabaseHelper.deleteFood("group123", foodId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Log.d("TestDeleteFood", "✅ 删除成功：" + foodId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestDeleteFood", "❌ 删除失败：" + e.getMessage());
            }
        });
    }

    public static void testAddMenu() {
        MenuItem menu = new MenuItem("西红柿炒蛋", Arrays.asList("西红柿", "鸡蛋"), "打蛋、炒蛋、加番茄炒匀");
        DatabaseHelper.addMenu("group123", menu, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Log.d("TestAddMenu", "✅ 添加菜单成功");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestAddMenu", "❌ 添加菜单失败：" + e.getMessage());
            }
        });
    }

    public static void testGetAllMenus() {
        DatabaseHelper.getAllMenus("group123", new FirebaseCallback<Map<String, MenuItem>>() {
            @Override
            public void onSuccess(Map<String, MenuItem> data) {
                for (Map.Entry<String, MenuItem> entry : data.entrySet()) {
                    String menuId = entry.getKey();
                    MenuItem menu = entry.getValue();
                    Log.d("TestMenu", "🍳 菜单 ID=" + menuId + "，名称=" + menu.name + "，食材=" + menu.ingredients);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestMenu", "❌ 获取失败：" + e.getMessage());
            }
        });
    }
    public static void testDeleteMenu(String groupId, String menuId) {
        DatabaseHelper.deleteMenu(groupId, menuId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Log.d("TestMenu", "✅ 成功删除菜单 ID=" + menuId);
                } else {
                    Log.w("TestMenu", "⚠️ 删除失败，但未抛异常（可能 ID 不存在？）");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestMenu", "❌ 删除菜单失败：" + e.getMessage());
            }
        });
    }


    public static void testCreateGroup() {
        DatabaseHelper.createGroup("group123", Arrays.asList("uid1", "uid2", "uid3"), new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Log.d("TestGroup", "✅ 小组创建成功");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestGroup", "❌ 创建失败：" + e.getMessage());
            }
        });
    }

    public static void testGetGroupMembers() {
        DatabaseHelper.getGroupMembers("group123", new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                Log.d("TestGroup", "👥 成员 UID：" + data);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestGroup", "❌ 获取失败：" + e.getMessage());
            }
        });
    }

    public static void insertSampleData() {
        // 插入多条食材
        List<FoodItem> foods = Arrays.asList(
                new FoodItem("黄瓜", Arrays.asList("uid1"), "2025-12-01", "冷藏", 5.0),
                new FoodItem("西红柿", Arrays.asList("uid1", "uid2"), "2025-11-30", "冷藏", 4.0),
                new FoodItem("鸡蛋", Arrays.asList("uid2"), "2025-12-03", "冷藏", 6.0),
                new FoodItem("面条", Arrays.asList("uid3"), "2025-12-10", "冷冻", 3.0),
                new FoodItem("牛奶", Arrays.asList("uid1"), "2025-11-28", "冷藏", 8.0)
        );

        for (FoodItem food : foods) {
            DatabaseHelper.addFood("group123", food, new FirebaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    Log.d("SampleAdd", "✅ 插入食材：" + food.name);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("SampleAdd", "❌ 插入失败：" + food.name + " - " + e.getMessage());
                }
            });
        }

        // 插入菜单
        List<MenuItem> menus = Arrays.asList(
                new MenuItem("西红柿炒鸡蛋", Arrays.asList("西红柿", "鸡蛋"), "打蛋→炒蛋→加番茄"),
                new MenuItem("凉拌黄瓜", Arrays.asList("黄瓜"), "切片→加醋→搅拌"),
                new MenuItem("面条鸡蛋汤", Arrays.asList("面条", "鸡蛋"), "煮面→打蛋→淋入搅拌")
        );

        for (MenuItem menu : menus) {
            DatabaseHelper.addMenu("group123", menu, new FirebaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    Log.d("SampleMenu", "✅ 插入菜单：" + menu.name);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("SampleMenu", "❌ 插入失败：" + menu.name + " - " + e.getMessage());
                }
            });
        }
    }
}
