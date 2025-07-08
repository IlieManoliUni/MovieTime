package ispw.project.movietime.bean;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserBean {

    private final StringProperty username;
    private final StringProperty password;

    private final StringProperty usernameError;
    private final StringProperty passwordError;

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 1;
    private static final int MAX_PASSWORD_LENGTH = 8;

    private static final String CHARACTERS_SUFFIX = " characters.";

    public UserBean() {
        this.username = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.usernameError = new SimpleStringProperty();
        this.passwordError = new SimpleStringProperty();
    }

    public UserBean(String username, String password) {
        this();
        this.username.set(username);
        this.password.set(password);
    }

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }

    public StringProperty usernameErrorProperty() { return usernameError; }
    public StringProperty passwordErrorProperty() { return passwordError; }

    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }

    public void setUsername(String username) {
        this.username.set(username);
        validateUsername();
    }

    public void setPassword(String password) {
        this.password.set(password);
        validatePassword();
    }

    private boolean validateUsername() {
        String currentUsername = getUsername();
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            usernameError.set("Username cannot be empty.");
            return false;
        }
        if (currentUsername.trim().length() < MIN_USERNAME_LENGTH) {
            usernameError.set("Username must be at least " + MIN_USERNAME_LENGTH + CHARACTERS_SUFFIX);
            return false;
        }
        if (currentUsername.trim().length() > MAX_USERNAME_LENGTH) {
            usernameError.set("Username cannot exceed " + MAX_USERNAME_LENGTH + CHARACTERS_SUFFIX);
            return false;
        }
        usernameError.set("");
        return true;
    }

    private boolean validatePassword() {
        String currentPassword = getPassword();
        if (currentPassword == null || currentPassword.isEmpty()) {
            passwordError.set("Password cannot be empty.");
            return false;
        }
        if (currentPassword.length() < MIN_PASSWORD_LENGTH) {
            passwordError.set("Password must be at least " + MIN_PASSWORD_LENGTH + CHARACTERS_SUFFIX);
            return false;
        }
        if (currentPassword.length() > MAX_PASSWORD_LENGTH) {
            passwordError.set("Password cannot exceed " + MAX_PASSWORD_LENGTH + CHARACTERS_SUFFIX);
            return false;
        }
        passwordError.set("");
        return true;
    }

    public boolean isValid() {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();
        return isUsernameValid && isPasswordValid;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "username='" + username.get() + '\'' +
                ", password='[HIDDEN]'" + // Always hide password in toString for security
                '}';
    }
}