package ispw.project.movietime.controller.application;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

public class SignupController {

    private UserDao userDao;
    private final ApplicationControllerProvider applicationControllerProvider = ApplicationControllerProvider.getInstance();

    public SignupController() {
        this.userDao = applicationControllerProvider.getUserDao();
    }

    public String signup(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "signup_form_view:Username cannot be empty.";
        }
        if (password == null || password.isEmpty()) {
            return "signup_form_view:Password cannot be empty.";
        }
        if (this.userDao == null) {
            return "signup_form_view:An internal system error occurred.";
        }

        try {
            UserModel existingUser = userDao.retrieveByUsername(username);
            if (existingUser != null) {
                return "signup_form_view:Username already exists. Please choose a different one.";
            }

            UserModel newUser = new UserModel(username, password);

            userDao.saveUser(newUser);

            SessionManager.getInstance().login(newUser);

            return "redirect:home";

        } catch (DaoException _) {
            return "signup_form_view:An error occurred during signup. Please try again later.";
        } catch (Exception e) {
            e.printStackTrace();
            return "signup_form_view:An unexpected system error occurred. Please try again.";
        }
    }
}