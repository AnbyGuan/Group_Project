package hk.hku.group_project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import hk.hku.group_project.MenuItem;
import hk.hku.group_project.R;
import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;

public class MenuAdapter extends ArrayAdapter<MenuItem> {
    private Context context;
    private List<MenuItem> menuItemList;

    private String groupID;


    public MenuAdapter(Context context, List<MenuItem> menuItemList, String groupID) {
        super(context, 0, menuItemList);
        this.context = context;
        this.menuItemList = menuItemList;
        this.groupID = groupID;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_menuitem, parent, false);
        }
        MenuItem menuItem = menuItemList.get(position);
        Log.i("MenuAdapter", "getView  menuItem: " + menuItem.toString());



        TextView name = convertView.findViewById(R.id.menu_name);
        TextView ingredients = convertView.findViewById(R.id.menu_ingredients);
        TextView steps = convertView.findViewById(R.id.menu_steps);
        ImageView delete = convertView.findViewById(R.id.delete);
        LinearLayout lin_bg = convertView.findViewById(R.id.lin_bg);

        name.setText(menuItem.getName());
        ingredients.setText("原料列表: " + menuItem.getIngredients());
        steps.setText("步骤: " + menuItem.getSteps());
        delete.setOnClickListener(v -> {
            deleteMenu(menuItem, groupID); // 先请求删除
            menuItemList.remove(position); // 本地移除
            notifyDataSetChanged(); // 通知 UI 更新
        });
        if (!menuItem.isReady()) {
            lin_bg.setBackgroundColor(Color.GRAY);
        }


        return convertView;
    }

    public void deleteMenu(MenuItem menuItem, String groupId) {
        String menuId = menuItem.getMenuId();
        if (menuId == null) {
            Toast.makeText(context, "菜单ID为空，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseHelper.deleteMenu(groupId, menuId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isSuccess) {
                if (isSuccess) {
                    Toast.makeText(context, "菜单删除成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                }
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "删除出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });
    }


}
