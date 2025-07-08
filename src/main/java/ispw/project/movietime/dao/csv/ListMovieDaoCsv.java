package ispw.project.movietime.dao.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListMovieDaoCsv implements ListMovie {

    private static final Logger LOGGER = Logger.getLogger(ListMovieDaoCsv.class.getName());

    private static final String CSV_FILE_NAME;
    private static final String MOVIE_CSV_FILE_NAME;

    static {
        Properties properties = new Properties();
        String listMovieFileName = "listmovie.csv";
        String movieFileName = "movie.csv";

        try (InputStream input = ListMovieDaoCsv.class.getClassLoader().getResourceAsStream("csv.properties")) {
            if (input != null) {
                properties.load(input);
                // --- MODIFIED LINES ---
                listMovieFileName = properties.getProperty("FILE_LIST_MOVIE", listMovieFileName); // Read "FILE_LIST_MOVIE"
                movieFileName = properties.getProperty("FILE_MOVIE", movieFileName);             // Read "FILE_MOVIE"
            } else {
                LOGGER.log(Level.WARNING, "csv.properties file not found. Using default CSV filenames: list_movie.csv=''{0}'', movie.csv=''{1}''",
                        new Object[]{listMovieFileName, movieFileName});
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Error loading csv.properties.", e); // Re-throw as CsvException
        }

        CSV_FILE_NAME = listMovieFileName;
        MOVIE_CSV_FILE_NAME = movieFileName;

        try {
            if (!Files.exists(Paths.get(CSV_FILE_NAME))) {
                Files.createFile(Paths.get(CSV_FILE_NAME));
                LOGGER.log(Level.INFO, "Created new CSV file for list-movie associations: {0}", CSV_FILE_NAME);
            }
            if (!Files.exists(Paths.get(MOVIE_CSV_FILE_NAME))) {
                Files.createFile(Paths.get(MOVIE_CSV_FILE_NAME));
                LOGGER.log(Level.INFO, "Created new CSV file for movies (referenced by ListMovieDaoCsv): {0}", MOVIE_CSV_FILE_NAME);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Could not create CSV files.", e);
        }
    }

    public ListMovieDaoCsv() {
        //Empty constructor
    }

    @Override
    public void addMovieToList(ListModel list, MovieModel movie) throws CsvException {
        try {
            if (movieExistsInList(list, movie)) {
                LOGGER.log(Level.WARNING, "Movie ID {0} already exists in list ID {1}. Not adding again.", new Object[]{movie.getId(), list.getId()});
                throw new CsvException("Movie ID " + movie.getId() + " already exists in list ID " + list.getId() + ".");
            }

            try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(Paths.get(CSV_FILE_NAME), StandardOpenOption.APPEND))) {
                String[] recordListMovie = {String.valueOf(list.getId()), String.valueOf(movie.getId())};
                csvWriter.writeNext(recordListMovie);
                LOGGER.log(Level.INFO, "Added movie ID {0} to list ID {1} in {2}.", new Object[]{movie.getId(), list.getId(), CSV_FILE_NAME});
            }
        } catch (IOException e) {
            throw new CsvException("Failed to add movie to list in CSV. I/O or data error.", e);
        }
    }

    @Override
    public void removeMovieFromList(ListModel list, MovieModel movie) throws CsvException {
        if (list == null || movie == null) {
            throw new IllegalArgumentException("List and Movie cannot be null for removal.");
        }

        java.nio.file.Path originalPath = Paths.get(CSV_FILE_NAME);
        java.nio.file.Path tempPath = Paths.get(CSV_FILE_NAME + ".tmp");
        List<String[]> allRecords = new ArrayList<>();
        boolean foundAndRemoved = false;

        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(originalPath))) {
            String[] recordListMovie;
            while ((recordListMovie = csvReader.readNext()) != null) {
                if (recordListMovie.length < 2) {
                    allRecords.add(recordListMovie);
                    continue;
                }
                if (recordListMovie[0].equals(String.valueOf(list.getId())) && recordListMovie[1].equals(String.valueOf(movie.getId()))) {
                    foundAndRemoved = true;
                } else {
                    allRecords.add(recordListMovie);
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to read CSV during movie removal. I/O or data error.", e);
        }

        if (!foundAndRemoved) {
            LOGGER.log(Level.WARNING, "Movie ID {0} not found in list ID {1} for removal attempt.", new Object[]{movie.getId(), list.getId()});
            throw new CsvException("Movie ID " + movie.getId() + " not found in list ID " + list.getId() + ".");
        }

        try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(tempPath))) {
            csvWriter.writeAll(allRecords);
        } catch (IOException e) {
            throw new CsvException("Failed to write temporary CSV file during movie removal. I/O error.", e);
        }

        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.log(Level.INFO, "Removed movie ID {0} from list ID {1} in {2}.", new Object[]{movie.getId(), list.getId(), CSV_FILE_NAME});
        } catch (IOException e) {
            throw new CsvException("Failed to replace original CSV file during movie removal. I/O error.", e);
        }
    }

    @Override
    public List<MovieModel> getAllMoviesInList(ListModel list) throws CsvException {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null for retrieving movies.");
        }
        List<MovieModel> movieList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordListMovie;
            while ((recordListMovie = csvReader.readNext()) != null) {
                if (recordListMovie.length < 2) {
                    LOGGER.log(Level.WARNING, "Skipping malformed list-movie record in CSV: not enough columns. Record: {0}", java.util.Arrays.toString(recordListMovie));
                    continue;
                }
                if (recordListMovie[0].equals(String.valueOf(list.getId()))) {
                    MovieModel movie = processListMovieRecordAndFetchMovie(recordListMovie, list.getId());
                    if (movie != null) {
                        movieList.add(movie);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to retrieve all movies for list from CSV. Data corruption or I/O error.", e);
        }
        LOGGER.log(Level.INFO, "Retrieved {0} movies for list ID {1} from {2}.", new Object[]{movieList.size(), list.getId(), CSV_FILE_NAME});
        return movieList;
    }

    private MovieModel processListMovieRecordAndFetchMovie(String[] recordListMovie, int listId) throws CsvException {
        try {
            int movieId = Integer.parseInt(recordListMovie[1]);
            MovieModel movie = fetchMovieById(movieId);
            if (movie != null) {
                return movie;
            } else {
                LOGGER.log(Level.WARNING, "Movie with ID {0} found in list ID {1} in {2}, but details not found in {3}. Skipping this entry. Record: {4}",
                        new Object[]{movieId, listId, CSV_FILE_NAME, MOVIE_CSV_FILE_NAME, java.util.Arrays.toString(recordListMovie)});
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Skipping malformed list-movie record in CSV due to invalid movie ID format. Record: {0}, Error: {1}",
                    new Object[]{java.util.Arrays.toString(recordListMovie), e.getMessage()});
        }
        return null;
    }

    @Override
    public void removeAllMoviesFromList(ListModel list) throws CsvException {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null.");
        }

        java.nio.file.Path originalPath = Paths.get(CSV_FILE_NAME);
        java.nio.file.Path tempPath = Paths.get(CSV_FILE_NAME + ".tmp");
        List<String[]> allRecordsToKeep = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(originalPath));
             CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(tempPath))) {

            String[] recordListMovie;
            boolean anyRemoved = false;
            while ((recordListMovie = csvReader.readNext()) != null) {
                if (recordListMovie.length < 2) {
                    allRecordsToKeep.add(recordListMovie);
                    continue;
                }
                if (!recordListMovie[0].equals(String.valueOf(list.getId()))) {
                    allRecordsToKeep.add(recordListMovie);
                } else {
                    anyRemoved = true;
                }
            }
            csvWriter.writeAll(allRecordsToKeep);
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);

            if (anyRemoved) {
                LOGGER.log(Level.INFO, "Removed all movies from list ID.");
            } else {
                LOGGER.log(Level.INFO, "No movies found for list ID in to remove.");
            }

        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to remove all movies from list in CSV. I/O or data error.", e);
        }
    }

    private boolean movieExistsInList(ListModel list, MovieModel movie) throws CsvException {
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordListMovie;
            while ((recordListMovie = csvReader.readNext()) != null) {
                if (recordListMovie.length < 2) {
                    continue;
                }
                if (recordListMovie[0].equals(String.valueOf(list.getId())) && recordListMovie[1].equals(String.valueOf(movie.getId()))) {
                    return true;
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to check movie existence in list from CSV. I/O or data error.", e);
        }
        return false;
    }

    private MovieModel fetchMovieById(int id) throws CsvException {
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(MOVIE_CSV_FILE_NAME)))) {
            String[] recordMovie;
            while ((recordMovie = csvReader.readNext()) != null) {
                if (recordMovie.length < 3) {
                    LOGGER.log(Level.WARNING, "Skipping malformed movie record during fetchMovieById: not enough columns. Record: {0}", java.util.Arrays.toString(recordMovie));
                    continue;
                }
                MovieModel movie = parseAndMatchMovieRecord(recordMovie, id);
                if (movie != null) {
                    return movie;
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to fetch movie details from main movie CSV file. I/O or data error.", e);
        }
        return null;
    }

    private MovieModel parseAndMatchMovieRecord(String[] recordMovie, int targetId) {
        try {
            int currentId = Integer.parseInt(recordMovie[0]);
            if (currentId == targetId) {
                int runtime = Integer.parseInt(recordMovie[1]);
                String title = recordMovie[2];
                return new MovieModel(currentId, runtime, title);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Skipping malformed movie record in CSV. Expected numeric ID or runtime, but found invalid data. Record: {0}, Error: {1}",
                    new Object[]{java.util.Arrays.toString(recordMovie), e.getMessage()});
        }
        return null;
    }
}