package ispw.project.movietime.connection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import ispw.project.movietime.exception.ApiException;
import ispw.project.movietime.model.MovieModel;

import java.util.ArrayList;
import java.util.List;

public class MovieJsonParser {

    private static final Gson gson = new Gson();

    private MovieJsonParser() {
        // This constructor is intentionally empty to prevent instantiation.
    }

    public static MovieModel parseMovie(String jsonResponse) throws ApiException {
        try {
            return gson.fromJson(jsonResponse, MovieModel.class);
        } catch (JsonSyntaxException e) {
            throw new ApiException("Failed to parse JSON into MovieModel: " + e.getMessage(), e);
        }
    }

    public static List<MovieModel> parseMovieSearchResults(String jsonResponse) throws ApiException {
        try {
            JsonObject rootJson = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray resultsArray = rootJson.getAsJsonArray("results");

            List<MovieModel> movies = new ArrayList<>();
            if (resultsArray != null) {
                for (int i = 0; i < resultsArray.size(); i++) {
                    MovieModel movie = gson.fromJson(resultsArray.get(i), MovieModel.class);
                    movies.add(movie);
                }
            }
            return movies;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Failed to parse JSON search results into List<MovieModel>: " + e.getMessage(), e);
        }
    }
}
