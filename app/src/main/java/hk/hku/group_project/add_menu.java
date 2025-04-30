package hk.hku.group_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.utils.UserSession;

public class add_menu extends AppCompatActivity {
    private static final String TAG = "add_menu_tag";
    private UserSession userSession;
    private String currentUserId;
    private List<String> groups = new ArrayList<>();
    private List<String> ingredients = new ArrayList<>();
    private RadioGroup groupRadioGroup;
    private Button addMenuButton;
    private EditText menu_name, ingredients_values, menu_steps;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);
        userSession = new UserSession(getApplicationContext());
        groupRadioGroup = findViewById(R.id.group_id);
        addMenuButton = findViewById(R.id.add_menu);
        menu_name = findViewById(R.id.menu_name);
        ingredients_values = findViewById(R.id.menu_ingredients);
        menu_steps = findViewById(R.id.menu_steps);

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
                Toast.makeText(getApplicationContext(), "fail to get the user_group", Toast.LENGTH_SHORT).show();

            }
        });

        addMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredientsInput = ingredients_values.getText().toString();
                String menuName = menu_name.getText().toString();
                String steps = menu_steps.getText().toString();
                String[] ingredientArray = ingredientsInput.split(",");
                ingredients.clear();
                for (String ingredient : ingredientArray) {
                    ingredients.add(ingredient.trim());
                }
                Log.d(TAG, "Ingredients: " + ingredients.toString());
                if (ingredients.isEmpty() || menuName.isEmpty() || steps.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter the complete menu information", Toast.LENGTH_SHORT).show();
                    return;
                }
                MenuItem menu = new MenuItem(menuName, ingredients, steps);
                int selectedRadioButtonId = groupRadioGroup.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    String selectedGroup = selectedRadioButton.getText().toString();
                    Log.d(TAG, "Selected Group: " + selectedGroup);
                    addMenu(selectedGroup, menu);
                } else {
                    Toast.makeText(add_menu.this, "Please select a group", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getUserGroups() {
        DatabaseHelper.getUserGroups(currentUserId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> groupIds) {
                groups = groupIds;
                // 清空原有的 RadioButton
                groupRadioGroup.removeAllViews();
                for (String group : groups) {
                    RadioButton radioButton = new RadioButton(add_menu.this);
                    radioButton.setText(group);
                    radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    groupRadioGroup.addView(radioButton);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }


    public void addMenu(String groupId, MenuItem menu) {
        DatabaseHelper.addMenu(groupId, menu, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                Toast.makeText(getApplicationContext(), "Menu added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to add menu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}