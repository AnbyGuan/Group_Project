package hk.hku.group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Open_Fridge extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_fridge);
    }

    public void add_food(View v) {
        Intent intent = new Intent(getBaseContext(), add_food.class);
        startActivity(intent);
    }
}