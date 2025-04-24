package hk.hku.group_project.database;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import hk.hku.group_project.database.FoodItem;
import hk.hku.group_project.MenuItem;
import hk.hku.group_project.R;

import java.util.Arrays;

public class TestActivity extends AppCompatActivity {

    Button btnAddFood, btnGetAllFood, btnDeleteFood, btnAddMenu, btnGetAllMenus, btnCreateGroup, btnGetGroupMembers, btnInsertSamples;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // 初始化组件
        btnAddFood = findViewById(R.id.btn_add_food);
        btnGetAllFood = findViewById(R.id.btn_get_foods);
        btnDeleteFood = findViewById(R.id.btn_delete_food);
        btnAddMenu = findViewById(R.id.btn_add_menu);
        btnGetAllMenus = findViewById(R.id.btn_get_menus);
        btnCreateGroup = findViewById(R.id.btn_create_group);
        btnGetGroupMembers = findViewById(R.id.btn_get_group_members);
        btnInsertSamples = findViewById(R.id.btn_insert_sample);
        txtResult = findViewById(R.id.txt_result);

        // 各功能测试
        btnAddFood.setOnClickListener(v -> {
            showResult("🍎 添加食材中...");
            DatabaseTest.testAddFood();
        });


        btnGetAllFood.setOnClickListener(v -> {
            showResult("📦 正在获取所有食材及 ID...");
            DatabaseTest.testGetAllFoodsWithIdToUI(this); // 新版带 ID 输出
        });


        btnDeleteFood.setOnClickListener(v -> {
            showResult("🗑️ 正在尝试删除指定 ID...");
            DatabaseTest.testDeleteFood("-OO_uwcILha8qT-ahN3H"); // 对应id
        });


        btnAddMenu.setOnClickListener(v -> {
            showResult("🍳 正在添加菜单...");
            DatabaseTest.testAddMenu();
        });

        btnGetAllMenus.setOnClickListener(v -> {
            showResult("📋 正在获取菜单...");
            DatabaseTest.testGetAllMenus();
        });

        btnCreateGroup.setOnClickListener(v -> {
            showResult("👥 正在创建小组...");
            DatabaseTest.testCreateGroup();
        });

        btnGetGroupMembers.setOnClickListener(v -> {
            showResult("🧑‍🤝‍🧑 正在获取小组成员...");
            DatabaseTest.testGetGroupMembers();
        });

        btnInsertSamples.setOnClickListener(v -> {
            showResult("📦 正在插入测试数据...");
            DatabaseTest.insertSampleData();
        });
    }

    public void showResult(String message) {
        txtResult.append("👉 " + message + "\n");
        Log.d("UI_RESULT", message);
    }
}
