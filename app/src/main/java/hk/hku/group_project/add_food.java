//package hk.hku.group_project;
//
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class add_food extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_food);
//
//
//    }
//}

package hk.hku.group_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.database.FoodItem;

public class add_food extends AppCompatActivity {

    Button btnTestAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 Firebase
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance(); // 可选，不用强制赋值

        setContentView(R.layout.activity_add_food);

        // 找到测试按钮
        btnTestAdd = findViewById(R.id.btn_test_add);

        btnTestAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> owners = Arrays.asList("uid1", "uid2");
                FoodItem testFood = new FoodItem("黄瓜", owners, "2025-12-01", "冷藏", 8.88);

                DatabaseHelper.addFood("group123", testFood, new FirebaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Log.d("AddFoodTest", "✅ 食材添加成功！");
                        Toast.makeText(add_food.this, "添加成功！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AddFoodTest", "❌ 添加失败：" + e.getMessage());
                        Toast.makeText(add_food.this, "添加失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
