package ispw.project.movietime.dao.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.MovieModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieDaoCsv implements MovieDao {

    private static final Logger LOGGER = Logger.getLogger(MovieDaoCsv.class.getName());

    private static final String CSV_FILE_NAME;

    private final HashMap<Integer, MovieModel> localCache;

    static {
        Properties properties = new Properties();
        String fileName = "movie.csv"; // Default filename

        try (InputStream input = MovieDaoCsv.class.getClassLoader().getResourceAsStream("csv.properties")) {
            if (input != null) {
                properties.load(input);
                fileName = properties.getProperty("FILE_MOVIE", fileName);
            } else {
                LOGGER.log(Level.WARNING, "csv.properties file not found. Using default filename: {0}", fileName);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Error loading csv.properties.", e);
        }
        CSV_FILE_NAME = fileName;

        try {
            if (!Files.exists(Paths.get(CSV_FILE_NAME))) {
                Files.createFile(Paths.get(CSV_FILE_NAME));
                LOGGER.log(Level.INFO, "Created new CSV file for movies: {0}", CSV_FILE_NAME);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Could not create CSV file for movies.", e);
        }
    }

    public MovieDaoCsv() {
        this.localCache = new HashMap<>();
    }

    @Override
    public MovieModel retrieveById(int id) throws CsvException {
        synchronized (localCache) {
            if (localCache.containsKey(id)) {
                return localCache.get(id);
            }
        }

        MovieModel movie = null;
        try {
            movie = retrieveByIdFromFile(id);
        } catch (IOException e) {
            throw new CsvException("Failed to retrieve movie from CSV for ID: " + id + ". I/O error.", e);
        } catch (NumberFormatException e) {
            throw new CsvException("Failed to retrieve movie from CSV for ID: " + id + ". Data corruption (invalid number format).", e);
        } catch (CsvValidationException e) {
            throw new CsvException("CSV data validation error while retrieving movie for ID: " + id, e);
        }

        if (movie != null) {
            synchronized (localCache) {
                localCache.put(id, movie);
            }
        }
        return movie;
    }

    private MovieModel retrieveByIdFromFile(int id) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordMovie;
            while ((recordMovie = csvReader.readNext()) != null) {
                if (recordMovie.length < 3) {
                    LOGGER.log(Level.WARNING, "Skipping malformed movie record during retrieveByIdFromFile: not enough columns. Record: {0}", java.util.Arrays.toString(recordMovie));
                    continue;
                }
                try {
                    int currentId = Integer.parseInt(recordMovie[0]);
                    if (currentId == id) {
                        return new MovieModel(currentId, Integer.parseInt(recordMovie[1]), recordMovie[2]);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Skipping malformed movie record during retrieveByIdFromFile due to invalid ID/runtime format. Record: {0}, Error: {1}",
                            new Object[]{java.util.Arrays.toString(recordMovie), e.getMessage()});
                }
            }
        }
        return null;
    }

    @Override
    public void saveMovie(MovieModel movie) throws CsvException {
        int movieId = movie.getId();

        synchronized (localCache) {
            if (localCache.containsKey(movieId)) {
                LOGGER.log(Level.WARNING, "Duplicated Movie ID {0} already in cache. Cannot save.", movieId);
                throw new CsvException("Duplicated Movie ID already in cache: " + movieId);
            }
        }

        MovieModel existingMovie = null;
        try {
            existingMovie = retrieveByIdFromFile(movieId);
        } catch (IOException | CsvValidationException e) {
            throw new CsvException("Failed to check existing movie for ID: " + movieId + ". I/O error.", e);
        } catch (NumberFormatException e) {
            throw new CsvException("Data corruption while checking existing movie for ID: " + movieId + ". Invalid number format.", e);
        }

        if (existingMovie != null) {
            LOGGER.log(Level.WARNING, "Duplicated Movie ID {0} already exists in CSV file. Cannot save.", movieId);
            throw new CsvException("Duplicated Movie ID already exists in CSV file: " + movieId);
        }

        try {
            saveMovieToFile(movie);
            LOGGER.log(Level.INFO, "Successfully saved new movie ''{0}'' with ID {1} to CSV.", new Object[]{movie.getTitle(), movie.getId()});
        } catch (IOException e) {
            throw new CsvException("Failed to save movie to CSV for ID: " + movieId + ". I/O error.", e);
        }

        synchronized (localCache) {
            localCache.put(movieId, movie);
        }
    }

    private void saveMovieToFile(MovieModel movie) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(Paths.get(CSV_FILE_NAME), StandardOpenOption.APPEND))) {
            String[] recordMovie = {String.valueOf(movie.getId()), String.valueOf(movie.getRuntime()), movie.getTitle()};
            csvWriter.writeNext(recordMovie);
        }
    }

    @Override
    public List<MovieModel> retrieveAllMovies() throws CsvException {
        List<MovieModel> movieList = new ArrayList<>();
        try {
            movieList = retrieveAllMoviesFromFile();
        } catch (IOException e) {
            throw new CsvException("Failed to retrieve all movies from CSV. I/O error.", e);
        } catch (CsvValidationException e) {
            throw new CsvException("CSV data validation error while retrieving all movies.", e);
        } catch (NumberFormatException e) {
            throw new CsvException("Data corruption while retrieving all movies. Invalid number format.", e);
        }

        synchronized (localCache) {
            localCache.clear();
            for (MovieModel movie : movieList) {
                localCache.put(movie.getId(), movie);
            }
        }

        LOGGER.log(Level.INFO, "Retrieved {0} movies from CSV.", movieList.size());
        return Collections.unmodifiableList(movieList);
    }

    private List<MovieModel> retrieveAllMoviesFromFile() throws IOException, CsvValidationException {
        List<MovieModel> movieList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordMovie;
            while ((recordMovie = csvReader.readNext()) != null) {
                if (recordMovie.length < 3) {
                    LOGGER.log(Level.WARNING, "Skipping malformed movie record during retrieveAllMoviesFromFile: not enough columns. Record: {0}", java.util.Arrays.toString(recordMovie));
                    continue;
                }
                try {
                    movieList.add(new MovieModel(Integer.parseInt(recordMovie[0]), Integer.parseInt(recordMovie[1]), recordMovie[2]));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Skipping malformed movie record during retrieveAllMoviesFromFile due to invalid ID/runtime format. Record: {0}, Error: {1}",
                            new Object[]{java.util.Arrays.toString(recordMovie), e.getMessage()});
                }
            }
        }
        return movieList;
    }
}