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
import java.util.ArrayList;

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
    public static void verifyLogin(String username, String password, FirebaseCallback<String> callback) {
        DatabaseReference ref = db.getReference("users").child(username);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String storedPw = snapshot.child("password").getValue(String.class);
                if (password.equals(storedPw)) {
                    callback.onSuccess(username);
                } else {
                    callback.onSuccess(null);
                }
            } else {
                callback.onSuccess(null);
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

    // 删除菜单
    public static void deleteMenu(String groupId, String menuId, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("menus").child(groupId).child(menuId);

        // Step 1: 检查该菜单是否存在
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Step 2: 存在就执行删除
                ref.removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                        .addOnFailureListener(callback::onFailure);
            } else {
                // 不存在，直接返回 false
                callback.onSuccess(false);
            }
        }).addOnFailureListener(callback::onFailure);
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

    // 搜索小组
    public static void searchGroups(String query, FirebaseCallback<Map<String, Object>> callback) {
        DatabaseReference ref = db.getReference("groups");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> result = new LinkedHashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String groupId = child.getKey();
                    if (groupId.toLowerCase().contains(query.toLowerCase())) {
                        // 将组 ID 和成员数量添加到结果中
                        long memberCount = child.child("members").getChildrenCount();
                        Map<String, Object> groupInfo = new HashMap<>();
                        groupInfo.put("memberCount", memberCount);
                        result.put(groupId, groupInfo);
                    }
                }
                callback.onSuccess(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    // 加入小组
    public static void joinGroup(String groupId, String userId, FirebaseCallback<Boolean> callback) {
        DatabaseReference groupRef = db.getReference("groups").child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 将用户添加到组
                    groupRef.child("members").child(userId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                // 将组添加到用户的组列表
                                DatabaseReference userGroupRef = db.getReference("user_groups").child(userId)
                                        .child(groupId);
                                userGroupRef.setValue(true)
                                        .addOnSuccessListener(aVoid2 -> callback.onSuccess(true))
                                        .addOnFailureListener(callback::onFailure);
                            })
                            .addOnFailureListener(callback::onFailure);
                } else {
                    callback.onSuccess(false); // 组不存在
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    // 离开小组
    public static void leaveGroup(String groupId, String userId, FirebaseCallback<Boolean> callback) {
        // 从组的成员列表中移除
        DatabaseReference groupMemberRef = db.getReference("groups").child(groupId).child("members").child(userId);
        groupMemberRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 从用户的组列表中移除
                    DatabaseReference userGroupRef = db.getReference("user_groups").child(userId).child(groupId);
                    userGroupRef.removeValue()
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess(true))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 获取用户加入的所有小组
    public static void getUserGroups(String userId, FirebaseCallback<List<String>> callback) {
        DatabaseReference ref = db.getReference("user_groups").child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> groups = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    groups.add(child.getKey());
                }
                callback.onSuccess(groups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

        // Get a ONE group that a user belongs to
        public static void getUserGroup(String userId, FirebaseCallback<String> callback) {
            DatabaseReference ref = db.getReference("user_groups").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        // Get the first (and only) group key since users can only be in one group
                        String groupId = snapshot.getChildren().iterator().next().getKey();
                        callback.onSuccess(groupId);
                    } else {
                        // User has no group
                        callback.onSuccess(null);
                    }
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFailure(error.toException());
                }
            });
        }

    // 检查小组是否存在
    public static void checkGroupExists(String groupId, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("groups").child(groupId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onSuccess(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    // Check if user is already in a group
    public static void isUserInAnyGroup(String userId, FirebaseCallback<Boolean> callback) {
        DatabaseReference ref = db.getReference("user_groups").child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the user has any groups, return true
                callback.onSuccess(snapshot.exists() && snapshot.getChildrenCount() > 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
}
