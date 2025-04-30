package hk.hku.group_project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hk.hku.group_project.R;
import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.database.FoodItem;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodList; // 改为final
    private final String currentGroupId;
    private final Context context;
    private static final int NORMAL = 0;
    private static final int ABOUT_TO_EXPIRE = 1;
    private static final int EXPIRED = 2;

    public FoodAdapter(List<FoodItem> foodList, String groupId, Context context) {
        this.foodList = foodList;
        this.currentGroupId = groupId;
        this.context = context;
    }


    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        FoodItem food = foodList.get(position);

        holder.foodName.setText(food.name);
        holder.foodExpiry.setText("保质期: " + food.expiry);
        holder.foodStorage.setText("存储方式: " + food.storage);
        holder.foodPrice.setText(String.format("价格: HK$%.2f", food.price));

        // 过期状态判断
        if (isAboutToExpire(food.expiry)) {
            // 即将过期样式
            holder.itemView.setBackgroundResource(R.drawable.bg_expiring_soon);
            holder.foodName.setTextColor(ContextCompat.getColor(context, R.color.warning_red));
            holder.foodExpiry.setTextColor(ContextCompat.getColor(context, R.color.warning_red));

            // 添加警告图标（左侧）
            Drawable warningIcon = ContextCompat.getDrawable(context, R.drawable.ic_warning);
            if (warningIcon != null) {
                warningIcon.setBounds(0, 0,
                        warningIcon.getIntrinsicWidth(),
                        warningIcon.getIntrinsicHeight());
                holder.foodExpiry.setCompoundDrawables(warningIcon, null, null, null);
            }
        } else {
            // 正常样式
            holder.itemView.setBackgroundResource(R.drawable.bg_normal);
            holder.foodName.setTextColor(Color.BLACK);
            holder.foodExpiry.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.foodExpiry.setCompoundDrawables(null, null, null, null); // 清除图标
        }


        // 显示所有归属人
        if (food.owners != null && !food.owners.isEmpty()) {
            StringBuilder ownersText = new StringBuilder("归属人: ");
            for (int i = 0; i < food.owners.size(); i++) {
                if (i > 0) ownersText.append(", ");
                ownersText.append(food.owners.get(i));
            }
            holder.foodOwners.setText(ownersText.toString());
        } else {
            holder.foodOwners.setText("归属人: 无");
        }

        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                FoodItem clickedFood = foodList.get(adapterPosition);
                showDeleteDialog(context, clickedFood, adapterPosition);
            }
        });

    }

    private boolean isAboutToExpire(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();

            long diff = expiry.getTime() - today.getTime();
            int daysLeft = (int) (diff / (1000 * 60 * 60 * 24));

            return daysLeft <= 3; // 3天内过期（包括今天）
        } catch (Exception e) {
            Log.e("FoodAdapter", "日期解析错误: " + expiryDate, e);
            return false;
        }
    }

    private int getExpiryStatus(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();

            long diff = expiry.getTime() - today.getTime();
            int daysLeft = (int) (diff / (1000 * 60 * 60 * 24));

            if (daysLeft < 0) return EXPIRED;       // 已过期
            if (daysLeft <= 3) return ABOUT_TO_EXPIRE; // 3天内过期
            return NORMAL;
        } catch (Exception e) {
            return NORMAL;
        }
    }

    private void setExpiredStyle(FoodViewHolder holder) {
        holder.itemView.setBackgroundResource(R.drawable.bg_expired);
        holder.foodName.setTextColor(ContextCompat.getColor(context, R.color.text_expired));
        holder.foodExpiry.setTextColor(ContextCompat.getColor(context, R.color.text_expired));

        // 设置过期图标（叉号）
        Drawable expiredIcon = ContextCompat.getDrawable(context, R.drawable.ic_expired);
        if (expiredIcon != null) {
            expiredIcon.setBounds(0, 0, expiredIcon.getIntrinsicWidth(), expiredIcon.getIntrinsicHeight());
            holder.foodExpiry.setCompoundDrawables(expiredIcon, null, null, null);
        }
    }


    // 添加更新列表方法
    public void updateList(List<FoodItem> newList) {
        this.foodList = new ArrayList<>(newList); // 创建新列表
        notifyDataSetChanged();
        Log.d("FoodAdapter", "数据更新，数量: " + foodList.size());
    }

    private void showDeleteDialog(Context context, FoodItem food, int position) {
        new AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("确定要删除 " + food.name + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteFood(position, food);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteFood(int position, FoodItem food) {
        DatabaseHelper.deleteFood(currentGroupId, food.id, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                foodList.remove(position);
                notifyItemRemoved(position);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "删除失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnDelete;
        TextView foodName, foodOwners, foodStorage, foodExpiry, foodPrice;

        public FoodViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
            foodExpiry = itemView.findViewById(R.id.foodExpiry);
            foodStorage = itemView.findViewById(R.id.foodStorage);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodOwners = itemView.findViewById(R.id.foodOwner);

            //绑定 btnDelete
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }





}