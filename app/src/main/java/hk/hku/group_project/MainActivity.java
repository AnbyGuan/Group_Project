package hk.hku.group_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.database.TestActivity;

public class MainActivity extends Activity {

    EditText txt_UserName, txt_UserPW;
    Button btn_Login, btn_Admin;
    TextView txtGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        txt_UserName = findViewById(R.id.txt_UserName);
        txt_UserPW = findViewById(R.id.txt_UserPW);
        btn_Login = findViewById(R.id.btn_Login);
        btn_Admin = findViewById(R.id.btn_admin_mode); // 在 layout 中添加一个按钮 id=btn_admin_mode

        btn_Login.setOnClickListener(v -> handleLogin());

        btn_Admin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TestActivity.class);
            startActivity(intent);
        });
        txtGoRegister = findViewById(R.id.link_to_register);
        txtGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }

    private void handleLogin() {
        String username = txt_UserName.getText().toString().trim();
        String password = txt_UserPW.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper.verifyLogin(username, password, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "✅ 登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MainPage.class); // 登录成功跳转主界面
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "❌ 用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "⚠️ 登录失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
