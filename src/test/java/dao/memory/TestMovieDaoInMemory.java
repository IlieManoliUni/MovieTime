package dao.memory;

import ispw.project.movietime.dao.memory.MovieDaoInMemory;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.MovieModel; // Make sure this import is correct
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestMovieDaoInMemory {

    private MovieDaoInMemory movieDaoInMemory;
    private Map<Integer, MovieModel> movieMap; // To access the private 'movieMap' via reflection

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        movieDaoInMemory = new MovieDaoInMemory();

        // Use reflection to access and clear the private 'movieMap' before each test
        Field movieMapField = MovieDaoInMemory.class.getDeclaredField("movieMap");
        movieMapField.setAccessible(true); // Allow access to private field
        movieMap = (Map<Integer, MovieModel>) movieMapField.get(movieDaoInMemory);
        movieMap.clear(); // Clear the map to ensure a clean state for each test
    }

    // --- retrieveById Tests ---

    @Test
    void testRetrieveByIdExistingMovie() throws DaoException {
        // Arrange
        // Using the new constructor: public MovieModel(int id, int runtime, String name)
        MovieModel existingMovie = new MovieModel(1, 148, "Inception");
        movieMap.put(existingMovie.getId(), existingMovie);

        // Act
        MovieModel retrievedMovie = movieDaoInMemory.retrieveById(1);

        // Assert
        assertNotNull(retrievedMovie, "Movie should be retrieved successfully.");
        assertEquals(existingMovie.getId(), retrievedMovie.getId(), "Retrieved movie ID should match.");
        assertEquals(existingMovie.getTitle(), retrievedMovie.getTitle(), "Retrieved movie title should match.");
        assertEquals(existingMovie.getRuntime(), retrievedMovie.getRuntime(), "Retrieved movie runtime should match.");
        // Note: Other fields (like year, director, genre) are not set by this constructor,
        // so we don't assert them here. If they are important for specific tests,
        // you would use setters or a more comprehensive constructor/builder.
    }

    @Test
    void testRetrieveByIdNonExistentMovie() throws DaoException {
        // Act
        MovieModel retrievedMovie = movieDaoInMemory.retrieveById(999); // Non-existent ID

        // Assert
        assertNull(retrievedMovie, "Should return null for a non-existent movie.");
    }

    // --- saveMovie Tests ---

    @Test
    void testSaveMovieSuccessfully() throws DaoException {
        // Arrange
        // Using the new constructor
        MovieModel newMovie = new MovieModel(101, 155, "Dune");

        // Act
        movieDaoInMemory.saveMovie(newMovie);

        // Assert
        assertTrue(movieMap.containsKey(101), "Movie should be added to the map.");
        assertEquals(newMovie, movieMap.get(101), "Stored movie object should be the same.");
    }

    @Test
    void testSaveMovieExistingIdThrowsDaoException() {
        // Arrange
        // Using the new constructor
        MovieModel existingMovie = new MovieModel(1, 100, "Old Movie");
        movieMap.put(existingMovie.getId(), existingMovie);

        // Using the new constructor
        MovieModel movieWithSameId = new MovieModel(1, 120, "New Movie"); // Same ID

        // Act & Assert
        DaoException thrown = assertThrows(DaoException.class,
                () -> movieDaoInMemory.saveMovie(movieWithSameId));
        assertEquals("Movie with ID 1 already exists. Use update if you intend to modify.", thrown.getMessage(), "Should throw DaoException for existing ID.");
        // Ensure the original movie object is still in the map (no overwrite)
        assertEquals("Old Movie", movieMap.get(1).getTitle(), "Existing movie's data should not be overwritten.");
    }

    @Test
    void testSaveMovieNullMovieThrowsIllegalArgumentException() {
        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> movieDaoInMemory.saveMovie(null));
        assertEquals("Movie model cannot be null.", thrown.getMessage());
    }

    // --- retrieveAllMovies Tests ---

    @Test
    void testRetrieveAllMoviesWhenEmpty() throws DaoException {
        // Act
        List<MovieModel> allMovies = movieDaoInMemory.retrieveAllMovies();

        // Assert
        assertNotNull(allMovies, "Should return an empty list, not null, when no movies are present.");
        assertTrue(allMovies.isEmpty(), "List should be empty when no movies are present.");
    }

    @Test
    void testRetrieveAllMoviesWithMultipleMovies() throws DaoException {
        // Arrange
        // Using the new constructor for all movies
        MovieModel movie1 = new MovieModel(1, 90, "Movie A");
        MovieModel movie2 = new MovieModel(2, 120, "Movie B");
        MovieModel movie3 = new MovieModel(3, 110, "Movie C");
        movieMap.put(movie1.getId(), movie1);
        movieMap.put(movie2.getId(), movie2);
        movieMap.put(movie3.getId(), movie3);

        // Act
        List<MovieModel> allMovies = movieDaoInMemory.retrieveAllMovies();

        // Assert
        assertNotNull(allMovies, "Should return a list of movies.");
        assertEquals(3, allMovies.size(), "Should retrieve all 3 movies.");
        assertTrue(allMovies.contains(movie1));
        assertTrue(allMovies.contains(movie2));
        assertTrue(allMovies.contains(movie3));
    }

    @Test
    void testRetrieveAllMoviesIsUnmodifiable() throws DaoException {
        // Arrange
        // Using the new constructor
        MovieModel movie1 = new MovieModel(1, 90, "Movie A");
        movieMap.put(movie1.getId(), movie1);

        // Act
        List<MovieModel> allMovies = movieDaoInMemory.retrieveAllMovies();

        // Assert
        // Attempt to add a new movie using the new constructor
        MovieModel newMovie = new MovieModel(4, 100, "New Movie");
        assertThrows(UnsupportedOperationException.class, () -> allMovies.add(newMovie),
                "The returned list should be unmodifiable.");
    }
}