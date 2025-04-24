package hk.hku.group_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;

public class RegisterActivity extends Activity {

    EditText txt_NewUser, txt_NewPW;
    CheckBox chk_IsAdmin;
    Button btn_Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_register);

        txt_NewUser = findViewById(R.id.txt_new_username);
        txt_NewPW = findViewById(R.id.txt_new_password);
        chk_IsAdmin = findViewById(R.id.chk_is_admin);
        btn_Register = findViewById(R.id.btn_register_submit);

        btn_Register.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String username = txt_NewUser.getText().toString().trim();
        String password = txt_NewPW.getText().toString().trim();
        boolean isAdmin = chk_IsAdmin.isChecked();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper.addUser(username, password, isAdmin, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Toast.makeText(RegisterActivity.this, "✅ 注册成功！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RegisterActivity.this, "❌ 注册失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
