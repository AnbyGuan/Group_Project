package hk.hku.group_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hk.hku.group_project.adapters.MemberAdapter;
import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.utils.UserSession;

public class GroupDetailsActivity extends AppCompatActivity {

    private TextView txtGroupId, txtMemberCount, txtNoMembers;
    private RecyclerView recyclerMembers;
    private Button btnLeaveGroup;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private MemberAdapter adapter;
    private String groupId;
    private String currentUserId;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Initialize UserSession and get current user ID
        userSession = new UserSession(getApplicationContext());
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = userSession.getUserId();

        // Get groupId from intent
        groupId = getIntent().getStringExtra("group_id");
        if (groupId == null) {
            Toast.makeText(this, "Group ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        txtGroupId = findViewById(R.id.txt_group_id_details);
        txtMemberCount = findViewById(R.id.txt_member_count_details);
        txtNoMembers = findViewById(R.id.txt_no_members);
        recyclerMembers = findViewById(R.id.recycler_members);
        btnLeaveGroup = findViewById(R.id.btn_leave_group);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar_details);

        // Setup RecyclerView
        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemberAdapter();
        recyclerMembers.setAdapter(adapter);

        // Set group ID
        txtGroupId.setText(groupId);

        // Setup button listeners
        btnBack.setOnClickListener(v -> finish());
        btnLeaveGroup.setOnClickListener(v -> confirmLeaveGroup());

        // Load group members
        loadGroupMembers();
    }

    private void loadGroupMembers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerMembers.setVisibility(View.GONE);
        txtNoMembers.setVisibility(View.GONE);

        DatabaseHelper.getGroupMembers(groupId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> members) {
                progressBar.setVisibility(View.GONE);

                if (members.isEmpty()) {
                    txtNoMembers.setVisibility(View.VISIBLE);
                    recyclerMembers.setVisibility(View.GONE);
                } else {
                    recyclerMembers.setVisibility(View.VISIBLE);
                    txtNoMembers.setVisibility(View.GONE);
                    adapter.setMembers(members);
                }

                txtMemberCount.setText(members.size() + " members");
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                txtNoMembers.setVisibility(View.VISIBLE);
                recyclerMembers.setVisibility(View.GONE);
                txtNoMembers.setText("Failed to load members: " + e.getMessage());
                Toast.makeText(GroupDetailsActivity.this, "Failed to load members: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLeaveGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Yes", (dialog, which) -> leaveGroup())
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveGroup() {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseHelper.leaveGroup(groupId, currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(GroupDetailsActivity.this, "Successfully left the group", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(GroupDetailsActivity.this, "Failed to leave the group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}