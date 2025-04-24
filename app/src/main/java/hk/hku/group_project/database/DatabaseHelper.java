package hk.hku.group_project.database;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hk.hku.group_project.MenuItem;

public class DatabaseHelper {

    private static final FirebaseDatabase db = FirebaseDatabase.getInstance();

    // 添加用户
    public static void addUser(String username, String password, boolean isAdmin, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("users").child(username);
        Map<String, Object> user = new HashMap<>();
        user.put("password", password);
        user.put("isAdmin", isAdmin);
        ref.setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // 用户登录验证
    public static void verifyLogin(String username, String password, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("users").child(username);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String storedPw = snapshot.child("password").getValue(String.class);
                if (password.equals(storedPw)) {
                    callback.onSuccess(true);
                } else {
                    callback.onSuccess(false);
                }
            } else {
                callback.onSuccess(false);
            }
        }).addOnFailureListener(callback::onFailure);
    }


    // 添加食材
    public static void addFood(String groupId, FoodItem food, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("foods").child(groupId);
        ref.push().setValue(food)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // 获取所有食材 + 返回 foodId
    public static void getAllFoodsWithId(String groupId, FirebaseCallback<Map<String, FoodItem>> callback) {
        DatabaseReference ref = db.getReference("foods").child(groupId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, FoodItem> result = new LinkedHashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FoodItem food = child.getValue(FoodItem.class);
                    String foodId = child.getKey();
                    result.put(foodId, food);
                }
                callback.onSuccess(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    // 删除食材
    public static void deleteFood(String groupId, String foodId, FirebaseCallback<Boolean> callback) {
        db.getReference("foods").child(groupId).child(foodId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // 添加菜单
    public static void addMenu(String groupId, MenuItem menu, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("menus").child(groupId);
        ref.push().setValue(menu)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // 获取所有菜单
    public static void getAllMenus(String groupId, FirebaseCallback<Map<String, MenuItem>> callback) {
        DatabaseReference ref = db.getReference("menus").child(groupId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, MenuItem> result = new LinkedHashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    MenuItem menu = child.getValue(MenuItem.class);
                    String menuId = child.getKey();
                    result.put(menuId, menu);
                }
                callback.onSuccess(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    // 创建小组（含多个 UID）
    public static void createGroup(String groupId, List<String> memberUids, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("groups").child(groupId).child("members");
        try {
            for (String uid : memberUids) {
                ref.child(uid).setValue(true);
            }
            callback.onSuccess(true);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    // 获取小组成员
    public static void getGroupMembers(String groupId, FirebaseCallback<List<String>> callback) {
        DatabaseReference ref = db.getReference("groups").child(groupId).child("members");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> members = new java.util.ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    members.add(child.getKey());
                }
                callback.onSuccess(members);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
}
