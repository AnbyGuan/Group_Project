package hk.hku.group_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

import hk.hku.group_project.database.DatabaseHelper;
import hk.hku.group_project.database.FirebaseCallback;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GROUP_ID = "groupId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSession(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.commit();

        // Update group ID from Firebase when user logs in
        updateGroupIdFromFirebase();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    /**
     * Get the user's group ID from local storage
     * 
     * @return The group ID or null if user isn't in a group
     */
    public String getGroupId() {
        return pref.getString(KEY_GROUP_ID, null);
    }

    /**
     * Save the group ID to local storage
     * 
     * @param groupId The group ID to save
     */
    public void saveGroupId(String groupId) {
        editor.putString(KEY_GROUP_ID, groupId);
        editor.commit();
    }

    /**
     * Check if user has a group
     * 
     * @return true if the user has a group, false otherwise
     */
    public boolean hasGroup() {
        return pref.getString(KEY_GROUP_ID, null) != null;
    }

    /**
     * Clear the stored group ID when user leaves a group
     */
    public void clearGroupId() {
        editor.remove(KEY_GROUP_ID);
        editor.commit();
    }

    /**
     * Update the locally stored group ID by fetching the latest from Firebase
     * Useful after login or when group changes might have occurred
     */
    public void updateGroupIdFromFirebase() {
        String userId = getUserId();
        if (userId != null) {
            DatabaseHelper.getUserGroup(userId, new FirebaseCallback<String>() {
                @Override
                public void onSuccess(String groupId) {
                    saveGroupId(groupId);
                }

                @Override
                public void onFailure(Exception e) {
                    // Keep using the existing stored group ID if fetch fails
                }
            });
        }
    }
}