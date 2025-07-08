package ispw.project.movietime.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    private List<ListModel> lists;

    public UserModel() {
        this.lists = new ArrayList<>();
    }

    public UserModel(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public UserModel(String username, String password, java.util.List<ListModel> lists) {
        this(username, password);
        this.lists = (lists != null) ? new ArrayList<>(lists) : new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public boolean verifyPassword(String providedPassword) {
        return this.password != null && this.password.equals(providedPassword);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public boolean addList(ListModel list) {
        if (list == null || this.lists.contains(list)) {
            return false;
        }
        return this.lists.add(list);
    }


    public boolean removeList(ListModel list) {
        return this.lists.remove(list);
    }

    public java.util.List<ListModel> getLists() {
        return Collections.unmodifiableList(lists);
    }

    public void setLists(java.util.List<ListModel> lists) {
        this.lists = (lists != null) ? new ArrayList<>(lists) : new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(username, userModel.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", numLists=" + lists.size() +
                '}';
    }
}