package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.controller.application.SignupController;
import ispw.project.movietime.exception.UserException;


public class SignUpCommand implements CliCommand {

    private final SignupController signupController;

    public SignUpCommand( ) {
        this.signupController = new SignupController();
    }

    @Override
    public String execute(String args) throws UserException {

        String[] signupArgs = args.trim().split(" ", 2);
        if (signupArgs.length < 2) {
            throw new UserException("Usage: signup <username> <password>");
        }

        String username = signupArgs[0];
        String password = signupArgs[1];

        String result = signupController.signup(username, password);

        if (result.startsWith("redirect:/login?message=SignupSuccessful")) {
            return "User '" + username + "' registered successfully. Please log in.";
        } else if (result.startsWith("signup_form_view:")) {
            String errorMessage = result.substring("signup_form_view:".length());
            throw new UserException("Registration failed: " + errorMessage);
        } else {
            throw new UserException("Registration failed due to an unexpected system error.");
        }
    }
}