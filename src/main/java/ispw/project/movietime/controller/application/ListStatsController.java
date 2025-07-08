package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.ListBean;
import ispw.project.movietime.bean.UserBean;

import ispw.project.movietime.controller.ApplicationControllerProvider;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;

import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.ListMovie;

import ispw.project.movietime.exception.DaoException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListStatsController {

    private static final Logger LOGGER = Logger.getLogger(ListStatsController.class.getName());

    private ListDao listDao;
    private ListMovie listMovieDao;

    private final ApplicationControllerProvider applicationControllerProvider= ApplicationControllerProvider.getInstance();

    public ListStatsController() {
        this.listDao = applicationControllerProvider.getListDao();
        this.listMovieDao = applicationControllerProvider.getListMovieDao();
    }

    public ListStatsResult getStatsForList(ListBean listBean, UserBean currentUserBean) throws DaoException {
        if (currentUserBean == null || currentUserBean.getUsername() == null || currentUserBean.getUsername().trim().isEmpty()) {
            throw new DaoException("User not authenticated or invalid user data. Cannot get list statistics.");
        }
        if (listBean == null || listBean.getId() <= 0) {
            throw new DaoException("Invalid list ID provided.");
        }

        try {
            UserModel currentUserModel = new UserModel(currentUserBean.getUsername(), null);
            ListModel listModel = listDao.retrieveById(listBean.getId());

            if (listModel == null) {
                LOGGER.log(Level.WARNING, "ListStatsController: List with ID {0} not found for user {1}",
                        new Object[]{listBean.getId(), currentUserBean.getUsername()});
                throw new DaoException("List not found.");
            }

            if (!listModel.getUsername().equals(currentUserModel.getUsername())) {
                throw new DaoException("You are not authorized to view statistics for this list.");
            }

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(listModel);

            int totalMovieRuntime = 0;
            int movieCount = 0;

            for (MovieModel movie : moviesInList) {
                totalMovieRuntime += movie.getRuntime();
                movieCount++;
            }

            return new ListStatsResult(
                    listModel.getId(),
                    listModel.getName(),
                    movieCount,
                    totalMovieRuntime
            );

        } catch (DaoException e) {
            throw new DaoException("Failed to retrieve list statistics due to a system error.", e);
        } catch (Exception e) {
            throw new DaoException("An unexpected system error occurred while retrieving statistics.", e);
        }
    }

    public static class ListStatsResult {
        private final int listId;
        private final String listName;
        private final int numberOfMovies;
        private final int totalRuntimeMinutes;

        public ListStatsResult(int listId, String listName, int numberOfMovies, int totalRuntimeMinutes) {
            this.listId = listId;
            this.listName = listName;
            this.numberOfMovies = numberOfMovies;
            this.totalRuntimeMinutes = totalRuntimeMinutes;
        }

        public int getListId() { return listId; }
        public String getListName() { return listName; }
        public int getNumberOfMovies() { return numberOfMovies; }
        public int getTotalRuntimeMinutes() { return totalRuntimeMinutes; }

        public String getFormattedTotalRuntime() {
            if (totalRuntimeMinutes <= 0) {
                return "0h 00m";
            }
            int hours = totalRuntimeMinutes / 60;
            int minutes = totalRuntimeMinutes % 60;
            return String.format("%dh %02dm", hours, minutes);
        }

        @Override
        public String toString() {
            return "ListStatsResult{" +
                    "listId=" + listId +
                    ", listName='" + listName + '\'' +
                    ", numberOfMovies=" + numberOfMovies +
                    ", totalRuntimeMinutes=" + totalRuntimeMinutes +
                    '}';
        }
    }
}