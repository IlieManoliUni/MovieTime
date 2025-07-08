package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;
import ispw.project.movietime.controller.application.ListStatsController;
import ispw.project.movietime.controller.application.ListStatsController.ListStatsResult;
import ispw.project.movietime.exception.UserException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.session.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ListStatsCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(ListStatsCommand.class.getName());


    private final ListStatsController listStatsController;

    public ListStatsCommand( ) {
        this.listStatsController = new ListStatsController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {

        UserBean currentUser = SessionManager.getInstance().getCurrentUserBean();

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "ListStatsCommand: Attempted to view stats without being logged in.");
            throw new UserException("You must be logged in to view list statistics.");
        }

        String[] parts = args.trim().split(" ");
        if (parts.length < 1 || parts[0].isEmpty()) {
            throw new UserException("Usage: liststats <listId>");
        }

        int listId;
        try {
            listId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException _) {
            throw new NumberFormatException("Invalid list ID. Please provide a valid number.");
        }

        if (listId <= 0) {
            LOGGER.log(Level.WARNING, "ListStatsCommand: List ID must be a positive number.");
            throw new UserException("List ID must be a positive number.");
        }

        try {
            ListBean listBean = new ListBean();
            listBean.setId(listId);


            ListStatsResult statsResult = listStatsController.getStatsForList(listBean, currentUser);

            StringBuilder sb = new StringBuilder();
            sb.append("--- Statistics for List '").append(statsResult.getListName()).append("' (ID: ").append(statsResult.getListId()).append(") ---\n\n");

            if (statsResult.getNumberOfMovies() > 0) {
                sb.append("--- Movies ---\n");
                sb.append("Total Movies: ").append(statsResult.getNumberOfMovies()).append("\n");
                sb.append("Total movie runtime: ").append(statsResult.getFormattedTotalRuntime()).append(".\n\n");
            } else {
                sb.append("--- No Movies in this list ---\n\n");
            }

            sb.append("Overall Total Runtime for list '").append(statsResult.getListName())
                    .append("': ").append(statsResult.getFormattedTotalRuntime()).append(".\n");

            sb.append("-------------------------------------------------------");
            return sb.toString();

        } catch (DaoException e) {
            throw new UserException("Failed to retrieve list statistics: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected error occurred while retrieving statistics. Please try again.", e);
        }
    }

}