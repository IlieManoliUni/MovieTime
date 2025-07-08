package ispw.project.movietime.connection;

import ispw.project.movietime.exception.ApiException;
import ispw.project.movietime.model.MovieModel;

import java.util.List;

public class TmdbApiFacade {

    private TmdbApiFacade() {
    }
    public static MovieModel getMovieById(int movieId) throws ApiException {
        String jsonResponse = TmdbApiClient.getMovieJsonById(movieId);

        return MovieJsonParser.parseMovie(jsonResponse);
    }

    public static List<MovieModel> searchMovies(String query) throws ApiException {
        return searchMovies(query, 1);
    }


    public static List<MovieModel> searchMovies(String query, int page) throws ApiException {
        String jsonResponse = TmdbApiClient.searchMoviesJson(query, page);

        return MovieJsonParser.parseMovieSearchResults(jsonResponse);
    }
}
