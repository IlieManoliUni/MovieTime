package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.CreateListController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateListCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(CreateListCommand.class.getName());

    private final CreateListController createListController;

    public CreateListCommand() {
        this.createListController = new CreateListController();
    }

    @Override
    public String execute(String args) throws UserException {
        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "CreateListCommand: Attempted to create list without being logged in.");
            throw new UserException("You must be logged in to create a list.");
        }
        String listName = args.trim();

        if (listName.isEmpty()) {
            LOGGER.log(Level.WARNING, "CreateListCommand: List name argument is empty.");
            throw new UserException("Please provide a name for the list. Usage: create_list <list_name>");
        }

        try {
            createListController.createNewList(listName, currentUser);

            return "List '" + listName + "' created successfully.";

        } catch (DaoException e) {
            throw new UserException("Failed to create list: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected error occurred while creating the list. Please try again.", e);
        }
    }
}