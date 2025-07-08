package ispw.project.movietime.session;

import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.bean.UserBean;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    private static SessionManager instance;

    private UserBean currentUserBean;

    private final ObjectProperty<UserBean> currentUserProperty = new SimpleObjectProperty<>();

    private SessionManager() {
        this.currentUserBean = null;
        this.currentUserProperty.set(null);
        LOGGER.log(Level.INFO, "SessionManager initialized. No user currently logged in.");
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(UserModel userModel) {
        if (userModel == null) {
            LOGGER.log(Level.WARNING, "Attempted to login with a null UserModel. Session not established.");
            return;
        }
        this.currentUserBean = new UserBean();
        this.currentUserBean.setUsername(userModel.getUsername());

        this.currentUserProperty.set(this.currentUserBean);
        LOGGER.log(Level.INFO, "User logged in. Session established and UI bindings updated.");
    }
    public void logout() {
        if (this.currentUserBean != null) {
            LOGGER.log(Level.INFO, "User logging out. Session terminated.");
        } else {
            LOGGER.log(Level.INFO, "Logout attempted, but no user was logged in.");
        }
        this.currentUserBean = null;
        this.currentUserProperty.set(null);
    }

    public UserBean getCurrentUserBean() {
        return currentUserBean;
    }

    public ObjectProperty<UserBean> currentUserBeanProperty() {
        return currentUserProperty;
    }

    public boolean isLoggedIn() {
        return currentUserBean != null;
    }
}