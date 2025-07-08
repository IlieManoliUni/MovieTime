package ispw.project.movietime.controller.graphic.cli.command;

import ispw.project.movietime.bean.MovieBean;
import ispw.project.movietime.controller.application.SeeMovieDetailsController;
import ispw.project.movietime.exception.UserException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SeeMovieDetailsCommand implements CliCommand {

    private static final Logger LOGGER = Logger.getLogger(SeeMovieDetailsCommand.class.getName());

    private final SeeMovieDetailsController seeMovieDetailsController;

    public SeeMovieDetailsCommand( ) {
        this.seeMovieDetailsController = new SeeMovieDetailsController();
    }

    @Override
    public String execute(String args) throws UserException, NumberFormatException {
        if (args.isEmpty()) {
            LOGGER.log(Level.WARNING, "SeeMovieDetailsCommand: Missing movie ID argument.");
            throw new UserException("Usage: seemoviedetails <movie_id>");
        }

        try {
            int movieId = Integer.parseInt(args.trim());

            MovieBean movie = seeMovieDetailsController.seeMovieDetails(movieId);

            StringBuilder sb = new StringBuilder();
            sb.append("--- Movie Details (ID: ").append(movie.getId()).append(") ---\n");
            sb.append("Title: ").append(movie.getTitle()).append("\n");
            sb.append("Original Title: ").append(movie.getOriginalTitle()).append(" (").append(movie.getOriginalLanguage()).append(")\n");
            sb.append("Overview: ").append(movie.getOverview()).append("\n");
            sb.append("Release Date: ").append(movie.getFormattedReleaseDate()).append("\n");
            sb.append("Runtime: ").append(movie.getRuntimeDisplay()).append("\n");
            sb.append("Average Vote: ").append(movie.getVoteAverageDisplay()).append(" (Count: ").append(movie.getVoteCountDisplay()).append(")\n");
            sb.append("Genres: ").append(movie.getGenresDisplay()).append("\n");
            sb.append("Production Companies: ").append(movie.getProductionCompaniesDisplay()).append("\n");
            sb.append("Spoken Languages: ").append(movie.getSpokenLanguagesDisplay()).append("\n");
            sb.append("Poster Path: ").append(movie.getFullPosterUrl()).append("\n");
            sb.append("--------------------------------------");

            LOGGER.log(Level.INFO, "SeeMovieDetailsCommand: Successfully retrieved details for movie ID {0}.", movieId);
            return sb.toString();

        } catch (NumberFormatException _) {
            throw new NumberFormatException("Invalid Movie ID. Please provide a valid integer ID.");
        } catch (Exception e) {
            throw new UserException("An unexpected system error occurred. Please try again.", e);
        }
    }
}