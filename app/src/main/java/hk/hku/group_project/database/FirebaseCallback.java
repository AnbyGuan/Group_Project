package hk.hku.group_project.database;

public interface FirebaseCallback<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}
