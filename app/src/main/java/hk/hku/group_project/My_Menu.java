package hk.hku.group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class My_Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_menu);
    }
    public void add_menu(View v) {
        Intent intent = new Intent(getBaseContext(), add_menu.class);
        startActivity(intent);
    }
}