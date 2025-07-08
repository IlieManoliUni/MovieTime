package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.DeleteListController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteListCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(DeleteListCommand.class.getName());

    private final DeleteListController deleteListController;

    public DeleteListCommand( ) {
        this.deleteListController = new DeleteListController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {
        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "DeleteListCommand: Attempted to delete list without being logged in.");
            throw new UserException("You must be logged in to delete a list.");
        }

        int listId;
        try {
            listId = Integer.parseInt(args.trim());
        } catch (NumberFormatException _) {
            throw new NumberFormatException("Invalid list ID. Please provide a valid number.");
        }

        if (listId <= 0) {
            LOGGER.log(Level.WARNING, "DeleteListCommand: List ID must be a positive number.");
            throw new UserException("List ID must be a positive number.");
        }

        try {
            ListBean listToDeleteBean = new ListBean();
            listToDeleteBean.setId(listId);


            deleteListController.deleteList(listToDeleteBean, currentUser);

           return "List with ID '" + listId + "' deleted successfully.";

        } catch (DaoException e) {
            throw new UserException("Failed to delete list: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected error occurred while deleting the list. Please try again.", e);
        }
    }
}