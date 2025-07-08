package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.session.SessionManager;

public class HelpCommand implements CliCommand {

    @Override
    public String execute(String args) {

        String loggedInCommands = "logout, searchmovie, seemoviedetails," +
                "createlist <list_name>, deletelist <list_id>, getalllists, " +
                "savemovietolist <list_id> <movie_id>, deletemoviefromlist <list_id> <movie_id>, ";

        String loggedOutCommands = "login <username> <password>, signup <username> <password>, " +
                "searchmovie <title>" + "seemoviedetails <movie_id>";

        StringBuilder helpText = new StringBuilder("--- Help ---\n");

        SessionManager sessionManager = SessionManager.getInstance();
        UserBean currentUser = sessionManager.getCurrentUserBean();

        if (sessionManager.isLoggedIn() && currentUser != null) {
            helpText.append("You are logged in as: ").append(currentUser.getUsername()).append("\n");
            helpText.append("Commands (logged in): ").append(loggedInCommands).append("\n");
        } else {
            helpText.append("You are logged out.\n");
            helpText.append("Commands (logged out): ").append(loggedOutCommands).append("\n");
        }
        helpText.append("Global Commands: help, clear, exit (processed by UI)\n");
        helpText.append("--- End Help ---");
        return helpText.toString();
    }
}