package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.SaveMovieToListController;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveMovieToListCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(SaveMovieToListCommand.class.getName());

    private final SaveMovieToListController saveMovieToListController;

    public SaveMovieToListCommand( ) {
        this.saveMovieToListController = new SaveMovieToListController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {

        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "SaveMovieToListCommand: Attempted to add movie to list without being logged in.");
            throw new UserException("You must be logged in to add movies to a list.");
        }

        String[] parts = args.trim().split(" ");
        if (parts.length < 2) {
            throw new UserException("Usage: savemovietolist <listId> <movieId>");
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
            LOGGER.log(Level.WARNING, "SaveMovieToListCommand: List ID and Movie ID must be positive numbers. ListId: {0}, MovieId: {1}", new Object[]{listId, movieId});
            throw new UserException("List ID and Movie ID must be positive numbers.");
        }

        ListBean targetListBean = new ListBean();
        targetListBean.setId(listId);

        MovieBean movieBeanToAdd = new MovieBean();
        movieBeanToAdd.setId(movieId);

        try {
            boolean added = saveMovieToListController.saveMovieToList(targetListBean, movieBeanToAdd, currentUser);

            if (added) {
                return "Movie with ID " + movieId + " added to list with ID '" + listId + "'.";
            } else {
                return "Movie with ID " + movieId + " is already in list with ID '" + listId + "'.";
            }

        } catch (DaoException e) {
            throw new UserException("Failed to add movie to list: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected system error occurred.", e);
        }
    }
}