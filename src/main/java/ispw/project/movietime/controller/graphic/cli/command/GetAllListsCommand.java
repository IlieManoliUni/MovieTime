package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.GetAllListsController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetAllListsCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(GetAllListsCommand.class.getName());

    private final GetAllListsController getAllListsController;

    public GetAllListsCommand( ) {
        this.getAllListsController = new GetAllListsController();
    }

    @Override
    public String execute(String args) throws UserException {
        if (!args.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "GetAllListsCommand received unexpected arguments. Ignoring.");
        }

        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "GetAllListsCommand: Attempted to get lists without being logged in.");
            throw new UserException("You must be logged in to view your lists.");
        }

        try {
            List<ListBean> lists = getAllListsController.getAllListsForUser(currentUser);

            if (lists.isEmpty()) {
                return "No lists found for the current user.";
            } else {
                StringBuilder sb = new StringBuilder("Your Lists:\n");
                for (ListBean list : lists) {
                    sb.append("  ID: ").append(list.getId()).append(", Name: '").append(list.getListName()).append("'\n"); // Corrected to getListName()
                }
                return sb.toString().trim();
            }

        } catch (DaoException e) {
            throw new UserException("Failed to retrieve your lists: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected error occurred while retrieving lists. Please try again.", e);
        }
    }
}