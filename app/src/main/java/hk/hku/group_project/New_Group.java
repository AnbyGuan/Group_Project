package hk.hku.group_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private Button btnSearch, btnCreateGroup, btnViewGroup;
    private RecyclerView recyclerSearchResults, recyclerMyGroups;
    private ProgressBar progressBar;
    private TextView txtNoGroups, txtUserInGroupMessage, txtGroupIdDisplay, txtMemberCountDisplay;
    private LinearLayout searchSection, createSection, noGroupContainer, hasGroupContainer;

    private GroupAdapter searchAdapter;
    private GroupAdapter myGroupsAdapter;
    private UserSession userSession;
    private String currentUserId;
    private boolean userAlreadyInGroup = false;
    private String currentGroupId = "";

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
        initializeViews();

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
        btnViewGroup.setOnClickListener(v -> {
            if (!currentGroupId.isEmpty()) {
                viewGroupDetails(currentGroupId);
            }
        });

        // Setup real-time search
        txtSearchGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !userAlreadyInGroup) {
                    searchGroups();
                } else {
                    recyclerSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Check if user is already in a group
        checkIfUserInGroup();
    }

    private void initializeViews() {
        txtSearchGroup = findViewById(R.id.txt_search_group);
        txtCreateGroupId = findViewById(R.id.txt_create_group_id);
        btnSearch = findViewById(R.id.btn_search);
        btnCreateGroup = findViewById(R.id.btn_create_group);
        btnViewGroup = findViewById(R.id.btn_view_group);
        recyclerSearchResults = findViewById(R.id.recycler_search_results);
        recyclerMyGroups = findViewById(R.id.recycler_my_groups);
        progressBar = findViewById(R.id.progress_bar);
        txtNoGroups = findViewById(R.id.txt_no_groups);
        txtUserInGroupMessage = findViewById(R.id.txt_user_in_group_message);
        txtGroupIdDisplay = findViewById(R.id.txt_group_id_display);
        txtMemberCountDisplay = findViewById(R.id.txt_member_count_display);

        searchSection = findViewById(R.id.search_section);
        createSection = findViewById(R.id.create_section);
        noGroupContainer = findViewById(R.id.no_group_container);
        hasGroupContainer = findViewById(R.id.has_group_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is already in a group
        checkIfUserInGroup();
    }

    private void checkIfUserInGroup() {
        progressBar.setVisibility(View.VISIBLE);

        // Hide both containers during loading
        noGroupContainer.setVisibility(View.GONE);
        hasGroupContainer.setVisibility(View.GONE);

        DatabaseHelper.isUserInAnyGroup(currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isInGroup) {
                userAlreadyInGroup = isInGroup;

                if (isInGroup) {
                    // User is already in a group, show "has group" container
                    noGroupContainer.setVisibility(View.GONE);
                    hasGroupContainer.setVisibility(View.VISIBLE);

                    // Load the user's group details
                    loadUserGroups();
                } else {
                    // User is not in a group, show "no group" container
                    noGroupContainer.setVisibility(View.VISIBLE);
                    hasGroupContainer.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                noGroupContainer.setVisibility(View.VISIBLE); // Show at least one container on error
                Toast.makeText(New_Group.this, "Error checking group status: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void searchGroups() {
        // If user is already in a group, don't allow searching
        if (userAlreadyInGroup) {
            Toast.makeText(this, "You are already in a group. Leave your current group to join a new one.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String query = txtSearchGroup.getText().toString().trim();
        if (query.isEmpty()) {
            recyclerSearchResults.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerSearchResults.setVisibility(View.GONE);

        // Hide the input fields and buttons during loading
        txtSearchGroup.setEnabled(false);
        btnSearch.setEnabled(false);
        txtCreateGroupId.setEnabled(false);
        btnCreateGroup.setEnabled(false);

        DatabaseHelper.searchGroups(query, new FirebaseCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> results) {
                progressBar.setVisibility(View.GONE);

                // Re-enable the input fields and buttons
                txtSearchGroup.setEnabled(true);
                btnSearch.setEnabled(true);
                txtCreateGroupId.setEnabled(true);
                btnCreateGroup.setEnabled(true);

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

                // Re-enable the input fields and buttons
                txtSearchGroup.setEnabled(true);
                btnSearch.setEnabled(true);
                txtCreateGroupId.setEnabled(true);
                btnCreateGroup.setEnabled(true);

                Toast.makeText(New_Group.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewGroup() {
        // If user is already in a group, don't allow creating a new one
        if (userAlreadyInGroup) {
            Toast.makeText(this, "You are already in a group. Leave your current group to create a new one.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = txtCreateGroupId.getText().toString().trim();
        if (groupId.isEmpty()) {
            Toast.makeText(this, "Please enter a group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Hide the input fields and buttons during loading
        txtSearchGroup.setEnabled(false);
        btnSearch.setEnabled(false);
        txtCreateGroupId.setEnabled(false);
        btnCreateGroup.setEnabled(false);

        // Check if group exists first
        DatabaseHelper.checkGroupExists(groupId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    progressBar.setVisibility(View.GONE);

                    // Re-enable the input fields and buttons
                    txtSearchGroup.setEnabled(true);
                    btnSearch.setEnabled(true);
                    txtCreateGroupId.setEnabled(true);
                    btnCreateGroup.setEnabled(true);

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

                                    // Re-enable the input fields and buttons (though they will be hidden by the
                                    // container change)
                                    txtSearchGroup.setEnabled(true);
                                    btnSearch.setEnabled(true);
                                    txtCreateGroupId.setEnabled(true);
                                    btnCreateGroup.setEnabled(true);

                                    Toast.makeText(New_Group.this, "Group created successfully", Toast.LENGTH_SHORT)
                                            .show();
                                    txtCreateGroupId.setText("");
                                    checkIfUserInGroup(); // Re-check and update UI
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    progressBar.setVisibility(View.GONE);

                                    // Re-enable the input fields and buttons
                                    txtSearchGroup.setEnabled(true);
                                    btnSearch.setEnabled(true);
                                    txtCreateGroupId.setEnabled(true);
                                    btnCreateGroup.setEnabled(true);

                                    Toast.makeText(New_Group.this, "Failed to join the new group: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);

                            // Re-enable the input fields and buttons
                            txtSearchGroup.setEnabled(true);
                            btnSearch.setEnabled(true);
                            txtCreateGroupId.setEnabled(true);
                            btnCreateGroup.setEnabled(true);

                            Toast.makeText(New_Group.this, "Failed to create group: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);

                // Re-enable the input fields and buttons
                txtSearchGroup.setEnabled(true);
                btnSearch.setEnabled(true);
                txtCreateGroupId.setEnabled(true);
                btnCreateGroup.setEnabled(true);

                Toast.makeText(New_Group.this, "Failed to check if group exists: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void loadUserGroups() {
        progressBar.setVisibility(View.VISIBLE);

        // No need to enable/disable controls in hasGroupContainer since it's already
        // visible at this point

        DatabaseHelper.getUserGroups(currentUserId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> groupIds) {
                if (groupIds.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    // Switch to no group view
                    userAlreadyInGroup = false;
                    noGroupContainer.setVisibility(View.VISIBLE);
                    hasGroupContainer.setVisibility(View.GONE);
                } else {
                    // We only care about the first group since users can only be in one group
                    String groupId = groupIds.get(0);
                    currentGroupId = groupId;

                    // Load this group's details
                    DatabaseHelper.getGroupMembers(groupId, new FirebaseCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> members) {
                            progressBar.setVisibility(View.GONE);

                            // Update the UI with the group details
                            txtGroupIdDisplay.setText("Group ID: " + groupId);
                            txtMemberCountDisplay.setText("Members: " + members.size());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(New_Group.this, "Failed to load group details: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(New_Group.this, "Failed to load your groups: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void loadGroupDetails(List<String> groupIds) {
        // This method is no longer needed with the new UI, but we'll keep it for
        // compatibility
    }

    private void joinGroup(String groupId) {
        // If user is already in a group, don't allow joining a new one
        if (userAlreadyInGroup) {
            Toast.makeText(this, "You are already in a group. Leave your current group to join a new one.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        DatabaseHelper.joinGroup(groupId, currentUserId, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(New_Group.this, "Successfully joined group " + groupId, Toast.LENGTH_SHORT).show();
                    recyclerSearchResults.setVisibility(View.GONE);
                    txtSearchGroup.setText("");
                    checkIfUserInGroup(); // Re-check and update UI
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
        // If user is already in a group, don't allow joining
        if (userAlreadyInGroup) {
            Toast.makeText(this, "You are already in a group. Leave your current group to join a new one.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Join Group")
                .setMessage("Do you want to join group '" + groupId + "'?")
                .setPositiveButton("Join", (dialog, which) -> joinGroup(groupId))
                .setNegativeButton("Cancel", null)
                .show();
    }
}