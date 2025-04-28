package hk.hku.group_project;

//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class MainPage extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main_page);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import hk.hku.group_project.utils.UserSession;

public class MainPage extends AppCompatActivity {

    private UserSession userSession;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize UserSession and check if user is logged in
        userSession = new UserSession(getApplicationContext());
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Add logout button
        btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userSession.logoutUser();
                    Toast.makeText(MainPage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainPage.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void NewGroup(View v) {
        Intent intent = new Intent(getBaseContext(), New_Group.class);
        startActivity(intent);
    }

    public void OpenFridge(View v) {
        Intent intent = new Intent(getBaseContext(), Open_Fridge.class);
        startActivity(intent);
    }

    public void MyMenu(View v) {
        Intent intent = new Intent(getBaseContext(), My_Menu.class);
        startActivity(intent);
    }

    public void Logout(View v) {
        confirmLogout();
    }
}