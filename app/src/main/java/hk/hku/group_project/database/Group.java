package hk.hku.group_project.database;

import java.util.List;

public class Group {
    public String groupId;
    public List<String> members;
    public long memberCount;

    public Group() {
    } // Required for Firebase

    public Group(String groupId, List<String> members) {
        this.groupId = groupId;
        this.members = members;
        this.memberCount = members != null ? members.size() : 0;
    }
}