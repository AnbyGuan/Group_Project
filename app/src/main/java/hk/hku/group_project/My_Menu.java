package hk.hku.group_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hk.hku.group_project.adapters.MenuAdapter;
import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.database.FoodItem;
import hk.hku.group_project.utils.UserSession;

public class My_Menu extends AppCompatActivity {
    private static final String TAG = "My_Menu_TAG";

    private ListView listView;
    private MenuAdapter adapter;
    private List<MenuItem> menuItemList = new ArrayList<>();
    private List<FoodItem> foodItemList = new ArrayList<>();
    private UserSession userSession;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_menu);
        userSession = new UserSession(getApplicationContext());
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = userSession.getUserId();
        //currentUserId = "uid1";  //测试数据

        DatabaseHelper.isUserInAnyGroup(currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isInGroup) {
                //是否已经在组中
                Log.i(TAG, "isInGroup: " + isInGroup);
                if (isInGroup) {
                    getUserGroups();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        listView = findViewById(R.id.listView);

    }

    public void add_menu(View v) {
        Intent intent = new Intent(getBaseContext(), add_menu.class);
        startActivity(intent);
    }

    public void getUserGroups() {
        DatabaseHelper.getUserGroups(currentUserId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> groupIds) {
                Log.i(TAG, "getUserGroups onSuccess: " + groupIds);
                if (!groupIds.isEmpty()) {
                    for (int i = 0; i < groupIds.size(); i++) {
                        //先查询食材
                        getFoodsByMenu(groupIds.get(i));
                        getMenusByGroup(groupIds.get(i));
                    }

                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    public void getMenusByGroup(String groupId) {
        DatabaseHelper.getAllMenus(groupId, new FirebaseCallback<Map<String, MenuItem>>() {
            @Override
            public void onSuccess(Map<String, MenuItem> data) {
                for (Map.Entry<String, MenuItem> entry : data.entrySet()) {
                    String menuId = entry.getKey();
                    MenuItem menu = entry.getValue();
                    Log.d(TAG, "🍳 menu ID=" + menuId + "，name=" + menu.name + "，food=" + menu.ingredients);

                    menu.setMenuId(menuId); // 👈 设置 menuId
                    boolean isDuplicate = false;
                    for (MenuItem existingMenu : menuItemList) {
                        if (existingMenu.name.equals(menu.name)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    // 如果不是重复项，则添加到 menuItemList 中
                    if (!isDuplicate) {
                        menuItemList.add(menu);
                    } else {
                        Log.i(TAG, "❌ The menu is duplicated：" + menu.name);
                    }
                }
                Log.i(TAG, "menuItemList : " + menuItemList);
                Log.i(TAG, "foodItemList : " + foodItemList);
                for (FoodItem foodItem : foodItemList){
                    Log.i(TAG, "MenuAdapter  foodItem: " + foodItem.name);
                }
                checkMenuItemsReadiness(menuItemList, foodItemList);
                adapter = new MenuAdapter(getApplicationContext(), menuItemList, groupId);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "❌ fail to get：" + e.getMessage());
            }
        });
    }


    public void getFoodsByMenu(String groupId) {
        DatabaseHelper.getAllFoodsWithId(groupId, new FirebaseCallback<Map<String, FoodItem>>() {
            @Override
            public void onSuccess(Map<String, FoodItem> data) {
                for (Map.Entry<String, FoodItem> entry : data.entrySet()) {
                    String foodId = entry.getKey();
                    FoodItem food = entry.getValue();
                    Log.i(TAG, "getFoodsByMenu  foodId=" + foodId + "，name=" + food.name + "，food_owners=" + food.owners);
                    for (int i = 0; i < food.owners.size(); i++) {
                        //如果食物的所有人包含当前用户，则添加到 foodItemList 中
                        if (food.owners.get(i).equals(currentUserId)) {
                            foodItemList.add(food);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public static void checkMenuItemsReadiness(List<MenuItem> menuItemList, List<FoodItem> foodItemList) {
        for (MenuItem menuItem : menuItemList) {
            boolean allIngredientsAvailable = true;
            for (String ingredient : menuItem.getIngredients()) {
                boolean ingredientFound = false;
                for (FoodItem foodItem : foodItemList) {
                    if (foodItem.name.equals(ingredient)) {
                        ingredientFound = true;
                        break;
                    }
                }
                if (!ingredientFound) {
                    allIngredientsAvailable = false;
                    break;
                }
            }
            menuItem.setReady(allIngredientsAvailable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 清空之前的菜单和食材列表
        menuItemList.clear();
        foodItemList.clear();
        // 重新加载数据
        DatabaseHelper.isUserInAnyGroup(currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isInGroup) {
                //是否已经在组中
                Log.i(TAG, "isInGroup onResume: " + isInGroup);
                if (isInGroup) {
                    getUserGroups();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }


}