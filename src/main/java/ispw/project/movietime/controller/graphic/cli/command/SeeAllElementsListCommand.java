package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.SeeAllElementsListController;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.session.SessionManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeeAllElementsListCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(SeeAllElementsListCommand.class.getName());

    private final SeeAllElementsListController seeAllElementsListController;

    public SeeAllElementsListCommand( ) {
        this.seeAllElementsListController = new SeeAllElementsListController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {

        SessionManager sessionManager = SessionManager.getInstance();
        UserBean currentUserBean = sessionManager.getCurrentUserBean();

        if (!sessionManager.isLoggedIn() || currentUserBean == null) {
            LOGGER.log(Level.WARNING, "SeeAllElementsListCommand: Attempted to view list elements without being logged in.");
            throw new UserException("You must be logged in to view list elements.");
        }

        String[] parts = args.trim().split(" ");
        if (parts.length < 1) {
            throw new UserException("Usage: seeallelementslist <listId>");
        }

        int listId;
        try {
            listId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException _) {
            throw new NumberFormatException("Invalid list ID. Please provide a valid number.");
        }

        if (listId <= 0) {
            LOGGER.log(Level.WARNING, "SeeAllElementsListCommand: List ID must be a positive number. ListId: {0}", listId);
            throw new UserException("List ID must be a positive number.");
        }

        ListBean listBean;
        List<MovieBean> movies;

        StringBuilder sb = new StringBuilder();

        try {
            listBean = seeAllElementsListController.getListDetailsForUser(listId, currentUserBean);

            sb.append("--- Elements in List '").append(listBean.getListName()).append("' (ID: ").append(listId).append(") ---\n");

            movies = seeAllElementsListController.seeAllMoviesInList(listId, currentUserBean);

            if (movies.isEmpty()) {
                sb.append("  (This list is empty of movies.)\n");
                LOGGER.log(Level.INFO, "CLI: List ID {0} for user {1} is empty of movies.", new Object[]{listId, currentUserBean.getUsername()});
            } else {
                appendMovies(sb, movies);
                LOGGER.log(Level.INFO, "CLI: Displaying {0} movies for list ID {1} for user {2}.",
                        new Object[]{movies.size(), listId, currentUserBean.getUsername()});
            }
        } catch (DaoException e) {
            throw new UserException("Error retrieving list elements: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected system error occurred while retrieving list elements. Please try again.", e);
        }

        sb.append("-------------------------------------------------------");
        return sb.toString();
    }

    private void appendMovies(StringBuilder sb, List<MovieBean> movies) {
        if (!movies.isEmpty()) {
            sb.append("  --- Movies ---\n");
            for (MovieBean movie : movies) {
                sb.append("    ID: ").append(movie.getId()).append(", Title: '").append(movie.getTitle()).append("'\n");
            }
        }
    }

}