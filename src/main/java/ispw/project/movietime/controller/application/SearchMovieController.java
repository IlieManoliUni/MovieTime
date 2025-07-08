package ispw.project.movietime.controller.application;

import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.connection.TmdbApiFacade;
import ispw.project.movietime.exception.ApiException;
import ispw.project.movietime.model.MovieModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchMovieController {

    public SearchMovieController() {
        // Default constructor
    }

    public List<MovieBean> searchMovies(String query) throws ApiException {
        return searchMovies(query, 1); // Delegate to the paginated method, default to page 1
    }

    public List<MovieBean> searchMovies(String query, int page) throws ApiException {
        if (query == null || query.trim().isEmpty()) {
            throw new ApiException("Search query cannot be empty.");
        }
        if (page <= 0) {
            throw new ApiException("Page number must be positive.");
        }

        List<MovieBean> movieBeans = new ArrayList<>();
        try {
            List<MovieModel> searchResultsModels = TmdbApiFacade.searchMovies(query, page);


            if (searchResultsModels != null && !searchResultsModels.isEmpty()) {
                for (MovieModel model : searchResultsModels) {
                    movieBeans.add(new MovieBean(model));
                }
            } else {
                return Collections.emptyList();
            }

            return movieBeans;

        } catch (ApiException e) {
           throw e;
        } catch (Exception e) {
            throw new ApiException("An unexpected system error occurred during movie search. Please try again.", e);
        }
    }
}