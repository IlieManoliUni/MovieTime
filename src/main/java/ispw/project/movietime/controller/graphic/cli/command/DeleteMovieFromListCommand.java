package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.DeleteMovieFromListController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteMovieFromListCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(DeleteMovieFromListCommand.class.getName());

    private final DeleteMovieFromListController deleteMovieFromListController;

    public DeleteMovieFromListCommand() {
        this.deleteMovieFromListController = new DeleteMovieFromListController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {

        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "DeleteMovieFromListCommand: Attempted to remove movie from list without being logged in.");
            throw new UserException("You must be logged in to remove movies from a list.");
        }

        String[] parts = args.trim().split(" ");
        if (parts.length < 2) {
            throw new UserException("Usage: deletemoviefromlist <listId> <movieId>");
        }

        int listId;
        int movieId;
        try {
            listId = Integer.parseInt(parts[0]);
            movieId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException _) {
            throw new NumberFormatException("Invalid list ID or movie ID. Please provide valid numbers.");
        }

        if (listId <= 0 || movieId <= 0) {
            LOGGER.log(Level.WARNING, "DeleteMovieFromListCommand: List ID and Movie ID must be positive numbers. ListId: {0}, MovieId: {1}", new Object[]{listId, movieId});
            throw new UserException("List ID and Movie ID must be positive numbers.");
        }

        try {
            boolean removed = deleteMovieFromListController.deleteMovieFromList(listId, movieId, currentUser);

            if (removed) {

                return "Movie with ID " + movieId + " removed from list with ID '" + listId + "'.";
            } else {
                return "Movie with ID " + movieId + " was not found in list with ID '" + listId + "'.";
            }

        } catch (DaoException e) {
            throw new UserException("Failed to remove movie from list: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected error occurred while removing the movie. Please try again.", e);
        }
    }
}