package hk.hku.group_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hk.hku.group_project.R;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<String> members;

    public MemberAdapter() {
        this.members = new ArrayList<>();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberId = members.get(position);
        holder.txtMemberId.setText(memberId);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void setMembers(List<String> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void addMember(String memberId) {
        if (!members.contains(memberId)) {
            members.add(memberId);
            notifyItemInserted(members.size() - 1);
        }
    }

    public void removeMember(String memberId) {
        int position = members.indexOf(memberId);
        if (position != -1) {
            members.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView txtMemberId;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMemberId = itemView.findViewById(R.id.txt_member_id);
        }
    }
}