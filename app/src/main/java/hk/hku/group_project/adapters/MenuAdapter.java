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

import androidx.annotation.NonNull;

import java.util.List;

import hk.hku.group_project.MenuItem;
import hk.hku.group_project.R;

public class MenuAdapter extends ArrayAdapter<MenuItem> {
    private Context context;
    private List<MenuItem> menuItemList;

    public MenuAdapter(Context context, List<MenuItem> menuItemList) {
        super(context, 0, menuItemList);
        this.context = context;
        this.menuItemList = menuItemList;
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
            menuItemList.remove(position);
            notifyDataSetChanged();

        });
        if (!menuItem.isReady()) {
            lin_bg.setBackgroundColor(Color.GRAY);
        }


        return convertView;
    }
}
