package hk.hku.group_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import hk.hku.group_project.adapters.GroupAdapter;
import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;
import hk.hku.group_project.database.Group;
import hk.hku.group_project.utils.UserSession;

public class New_Group extends AppCompatActivity implements GroupAdapter.OnGroupClickListener {

    private EditText txtSearchGroup, txtCreateGroupId;
    private Button btnSearch, btnCreateGroup;
    private RecyclerView recyclerSearchResults, recyclerMyGroups;
    private ProgressBar progressBar;
    private TextView txtNoGroups;

    private GroupAdapter searchAdapter;
    private GroupAdapter myGroupsAdapter;
    private UserSession userSession;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        // Initialize UserSession and get current user ID
        userSession = new UserSession(getApplicationContext());
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        currentUserId = userSession.getUserId();

        // Initialize views
        txtSearchGroup = findViewById(R.id.txt_search_group);
        txtCreateGroupId = findViewById(R.id.txt_create_group_id);
        btnSearch = findViewById(R.id.btn_search);
        btnCreateGroup = findViewById(R.id.btn_create_group);
        recyclerSearchResults = findViewById(R.id.recycler_search_results);
        recyclerMyGroups = findViewById(R.id.recycler_my_groups);
        progressBar = findViewById(R.id.progress_bar);
        txtNoGroups = findViewById(R.id.txt_no_groups);

        // Setup RecyclerViews
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyGroups.setLayoutManager(new LinearLayoutManager(this));

        searchAdapter = new GroupAdapter(this, false);
        myGroupsAdapter = new GroupAdapter(this, true);

        recyclerSearchResults.setAdapter(searchAdapter);
        recyclerMyGroups.setAdapter(myGroupsAdapter);

        // Setup button listeners
        btnSearch.setOnClickListener(v -> searchGroups());
        btnCreateGroup.setOnClickListener(v -> createNewGroup());

        // Setup real-time search
        txtSearchGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchGroups();
                } else {
                    recyclerSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Load user's groups
        loadUserGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user's groups when returning to this screen
        loadUserGroups();
    }

    private void searchGroups() {
        String query = txtSearchGroup.getText().toString().trim();
        if (query.isEmpty()) {
            recyclerSearchResults.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerSearchResults.setVisibility(View.GONE);

        DatabaseHelper.searchGroups(query, new FirebaseCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> results) {
                progressBar.setVisibility(View.GONE);

                if (results.isEmpty()) {
                    Toast.makeText(New_Group.this, "No groups found matching '" + query + "'", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    recyclerSearchResults.setVisibility(View.VISIBLE);
                    searchAdapter.updateFromSearchResults(results);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(New_Group.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewGroup() {
        String groupId = txtCreateGroupId.getText().toString().trim();
        if (groupId.isEmpty()) {
            Toast.makeText(this, "Please enter a group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check if group exists first
        DatabaseHelper.checkGroupExists(groupId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(New_Group.this, "Group ID already exists. Please choose another ID.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Create new group with current user as member
                    DatabaseHelper.createGroup(groupId, Arrays.asList(currentUserId), new FirebaseCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            // Update user_groups to track that this user is in this group
                            DatabaseHelper.joinGroup(groupId, currentUserId, new FirebaseCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean joinSuccess) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(New_Group.this, "Group created successfully", Toast.LENGTH_SHORT)
                                            .show();
                                    txtCreateGroupId.setText("");
                                    loadUserGroups();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(New_Group.this, "Failed to join the new group: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(New_Group.this, "Failed to create group: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(New_Group.this, "Failed to check if group exists: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void loadUserGroups() {
        progressBar.setVisibility(View.VISIBLE);
        txtNoGroups.setVisibility(View.GONE);
        recyclerMyGroups.setVisibility(View.GONE);

        DatabaseHelper.getUserGroups(currentUserId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> groupIds) {
                if (groupIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    txtNoGroups.setVisibility(View.VISIBLE);
                    recyclerMyGroups.setVisibility(View.GONE);
                } else {
                    loadGroupDetails(groupIds);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                txtNoGroups.setVisibility(View.VISIBLE);
                recyclerMyGroups.setVisibility(View.GONE);
                Toast.makeText(New_Group.this, "Failed to load your groups: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void loadGroupDetails(List<String> groupIds) {
        List<Group> groups = new ArrayList<>();
        final int[] groupsToLoad = { groupIds.size() };

        for (String groupId : groupIds) {
            DatabaseHelper.getGroupMembers(groupId, new FirebaseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> members) {
                    Group group = new Group(groupId, members);
                    groups.add(group);

                    groupsToLoad[0]--;
                    if (groupsToLoad[0] == 0) {
                        // All groups loaded
                        progressBar.setVisibility(View.GONE);
                        txtNoGroups.setVisibility(View.GONE);
                        recyclerMyGroups.setVisibility(View.VISIBLE);
                        myGroupsAdapter.setGroups(groups);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    groupsToLoad[0]--;
                    if (groupsToLoad[0] == 0) {
                        // All groups attempted to load
                        progressBar.setVisibility(View.GONE);
                        if (groups.isEmpty()) {
                            txtNoGroups.setVisibility(View.VISIBLE);
                            recyclerMyGroups.setVisibility(View.GONE);
                        } else {
                            txtNoGroups.setVisibility(View.GONE);
                            recyclerMyGroups.setVisibility(View.VISIBLE);
                            myGroupsAdapter.setGroups(groups);
                        }
                    }
                }
            });
        }
    }

    private void joinGroup(String groupId) {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseHelper.joinGroup(groupId, currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(New_Group.this, "Successfully joined group " + groupId, Toast.LENGTH_SHORT).show();
                    recyclerSearchResults.setVisibility(View.GONE);
                    txtSearchGroup.setText("");
                    loadUserGroups();
                } else {
                    Toast.makeText(New_Group.this, "Failed to join group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(New_Group.this, "Error joining group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewGroupDetails(String groupId) {
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("group_id", groupId);
        startActivity(intent);
    }

    @Override
    public void onActionClick(String groupId) {
        // For search results: Join button clicked
        // For my groups: View button clicked
        if (recyclerSearchResults.getVisibility() == View.VISIBLE &&
                txtSearchGroup.getText().toString().trim().length() > 0) {
            confirmJoinGroup(groupId);
        } else {
            viewGroupDetails(groupId);
        }
    }

    @Override
    public void onItemClick(String groupId) {
        // For both lists: Navigate to group details
        viewGroupDetails(groupId);
    }

    private void confirmJoinGroup(String groupId) {
        new AlertDialog.Builder(this)
                .setTitle("Join Group")
                .setMessage("Do you want to join group '" + groupId + "'?")
                .setPositiveButton("Join", (dialog, which) -> joinGroup(groupId))
                .setNegativeButton("Cancel", null)
                .show();
    }
}