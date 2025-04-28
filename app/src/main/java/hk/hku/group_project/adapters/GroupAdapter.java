package hk.hku.group_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hk.hku.group_project.R;
import hk.hku.group_project.database.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private OnGroupClickListener listener;
    private boolean isJoined;

    public interface OnGroupClickListener {
        void onActionClick(String groupId);

        void onItemClick(String groupId);
    }

    public GroupAdapter(OnGroupClickListener listener, boolean isJoined) {
        this.groups = new ArrayList<>();
        this.listener = listener;
        this.isJoined = isJoined;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.txtGroupId.setText(group.groupId);
        holder.txtMemberCount.setText("Members: " + group.memberCount);

        if (isJoined) {
            holder.btnAction.setText("View");
            holder.btnAction.setBackgroundTintList(
                    holder.itemView.getContext().getColorStateList(android.R.color.holo_blue_dark));
        } else {
            holder.btnAction.setText("Join");
            holder.btnAction.setBackgroundTintList(
                    holder.itemView.getContext().getColorStateList(android.R.color.holo_green_dark));
        }

        holder.btnAction.setOnClickListener(v -> listener.onActionClick(group.groupId));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(group.groupId));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void updateFromSearchResults(Map<String, Object> searchResults) {
        groups.clear();
        for (Map.Entry<String, Object> entry : searchResults.entrySet()) {
            String groupId = entry.getKey();
            Map<String, Object> groupInfo = (Map<String, Object>) entry.getValue();
            long memberCount = (long) groupInfo.get("memberCount");

            Group group = new Group(groupId, null);
            group.memberCount = memberCount;
            groups.add(group);
        }
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView txtGroupId, txtMemberCount;
        Button btnAction;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGroupId = itemView.findViewById(R.id.txt_group_id);
            txtMemberCount = itemView.findViewById(R.id.txt_member_count);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}