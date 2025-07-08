package ispw.project.movietime.dao.memory;

import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.exception.DaoException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDaoInMemory implements UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDaoInMemory.class.getName());

    private final Map<String, UserModel> users = new HashMap<>();
    static {

        try {
            UserModel demoUser1 = new UserModel("demoUser", "demoPass123");
            (new UserDaoInMemory()).users.put(demoUser1.getUsername(), demoUser1);

            UserModel testUser = new UserModel("testUser", "testPass456");
            (new UserDaoInMemory()).users.put(testUser.getUsername(), testUser);

            LOGGER.log(Level.INFO, "Initialized {0} demo users in memory.", (new UserDaoInMemory()).users.size());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error initializing demo users: {0}", e.getMessage());
        }
    }

    @Override
    public UserModel retrieveByUsername(String username) throws DaoException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        LOGGER.log(Level.INFO, "Attempting to retrieve user: {0}", username);
        return users.get(username);
    }

    @Override
    public void saveUser(UserModel user) throws DaoException {
        if (user == null) {
            throw new IllegalArgumentException("User model cannot be null.");
        }
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("User's username cannot be null or empty.");
        }

        if (users.containsKey(username)) {
            LOGGER.log(Level.WARNING, "Attempted to save user but username already exists.");
            throw new DaoException("Username already exists: " + username);
        }

        users.put(username, user);
        LOGGER.log(Level.INFO, "User saved successfully to in-memory storage.");
    }
}