package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.connection.TmdbApiFacade;
import ispw.project.movietime.exception.ApiException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SeeMovieDetailsController {

    private static final Logger LOGGER = Logger.getLogger(SeeMovieDetailsController.class.getName());

    public SeeMovieDetailsController() {
        // Constructor is empty
    }


    public MovieBean seeMovieDetails(int movieId) throws ApiException {
        if (movieId <= 0) {
            LOGGER.log(Level.WARNING, "SeeMovieDetailsController: Invalid movie ID provided: {0}", movieId);
            throw new ApiException("Invalid movie ID provided. Movie ID must be positive.");
        }

        try {
            MovieModel movieModel = TmdbApiFacade.getMovieById(movieId);

            if (movieModel == null) {
                LOGGER.log(Level.WARNING, "SeeMovieDetailsController: Movie with ID {0} not found via API, even after facade call.", movieId);
                throw new ApiException("Movie with ID " + movieId + " not found in external API.");
            }

            return new MovieBean(movieModel);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("An unexpected system error occurred while fetching movie details.", e);
        }
    }
}