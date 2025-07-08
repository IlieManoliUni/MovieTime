package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.controller.application.SearchMovieController;
import ispw.project.movietime.exception.ApiException;
import ispw.project.movietime.exception.UserException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchMovieCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(SearchMovieCommand.class.getName());

    private final SearchMovieController searchMovieController;

    public SearchMovieCommand( ) {
        this.searchMovieController = new SearchMovieController();
    }

    @Override
    public String execute(String args) throws UserException {

        String query = args.trim();
        if (query.isEmpty()) {
            LOGGER.log(Level.WARNING, "SearchMovieCommand: No search query provided.");
            throw new UserException("Usage: searchmovie <query>");
        }

        StringBuilder sb = new StringBuilder();
        try {
            List<MovieBean> movies = searchMovieController.searchMovies(query);

            if (movies.isEmpty()) {
                sb.append("No movies found for query: '").append(query).append("'\n");
            } else {
                sb.append("--- Search Results for '").append(query).append("' ---\n\n");
                for (MovieBean movie : movies) {
                    sb.append("ID: ").append(movie.getId()).append("\n");
                    sb.append("Title: ").append(movie.getTitle()).append("\n");
                    sb.append("Release Date: ").append(movie.getFormattedReleaseDate()).append("\n");
                    sb.append("Runtime: ").append(movie.getRuntimeDisplay()).append("\n");
                    sb.append("Overview: ").append(
                            movie.getOverview().length() > 100 ?
                                    movie.getOverview().substring(0, 100) + "..." :
                                    movie.getOverview()
                    ).append("\n");
                    sb.append("---------------------------------------------------\n");
                }
            }
            return sb.toString();

        } catch (ApiException e) {
            throw new UserException("Failed to search movies: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new UserException("An unexpected system error occurred during movie search. Please try again.", e);
        }
    }
}