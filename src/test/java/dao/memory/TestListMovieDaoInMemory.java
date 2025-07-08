package dao.memory;

import ispw.project.movietime.dao.memory.ListMovieDaoInMemory;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ListMovieDaoInMemory Tests")
class TestListMovieDaoInMemory {

    private ListMovieDaoInMemory listMovieDao;

    // Helper method to create a ListModel for testing
    // Updated to match the new ListModel constructor
    private ListModel createList(int id, String name, String username) {
        return new ListModel(id, name, username);
    }

    // Helper method to create a MovieModel for testing
    // Using the new constructor in MovieModel
    private MovieModel createMovie(int id, String title) {
        // We can use the constructor that takes id, runtime, and name (title)
        // For simplicity in tests, runtime can be a dummy value if not specifically tested.
        return new MovieModel(id, 120, title);
    }

    @BeforeEach
    void setUp() {
        listMovieDao = new ListMovieDaoInMemory();
    }

    // --- Tests for addMovieToList ---
    @Nested
    @DisplayName("Add Movie to List Operations")
    class AddMovieToListTests {

        @Test
        @DisplayName("Should add a movie to an empty list")
        void testAddMovieToEmptyList() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");

            listMovieDao.addMovieToList(list1, movie1);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList, "Movies list should not be null");
            assertEquals(1, moviesInList.size(), "List should contain one movie");
            assertTrue(moviesInList.contains(movie1), "List should contain the added movie");
        }

        @Test
        @DisplayName("Should add multiple movies to the same list")
        void testAddMultipleMoviesToList() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");
            MovieModel movie2 = createMovie(102, "Interstellar");

            listMovieDao.addMovieToList(list1, movie1);
            listMovieDao.addMovieToList(list1, movie2);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList);
            assertEquals(2, moviesInList.size(), "List should contain two movies");
            assertTrue(moviesInList.contains(movie1), "List should contain movie1");
            assertTrue(moviesInList.contains(movie2), "List should contain movie2");
        }

        @Test
        @DisplayName("Should throw DaoException when adding the same movie twice to the same list")
        void testAddDuplicateMovieThrowsDaoException() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");

            listMovieDao.addMovieToList(list1, movie1);

            // Assert that adding the duplicate throws DaoException
            DaoException thrown = assertThrows(DaoException.class, () -> {
                listMovieDao.addMovieToList(list1, movie1);
            }, "Expected DaoException for duplicate movie add");

            // Optionally, check the exception message
            assertTrue(thrown.getMessage().contains("already exists"), "Exception message should indicate duplicate");

            // Verify that the movie is still in the list exactly once
            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertEquals(1, moviesInList.size(), "List should still contain only one movie after duplicate attempt");
            assertTrue(moviesInList.contains(movie1), "The original movie should still be in the list");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when adding a movie with null ListModel")
        void testAddMovieWithNullListThrowsIllegalArgumentException() {
            MovieModel movie1 = createMovie(101, "Inception");
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.addMovieToList(null, movie1),
                    "Expected IllegalArgumentException for null ListModel");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when adding a movie with null MovieModel")
        void testAddMovieWithNullMovieThrowsIllegalArgumentException() {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.addMovieToList(list1, null),
                    "Expected IllegalArgumentException for null MovieModel");
        }
    }

    // --- Tests for removeMovieFromList ---
    @Nested
    @DisplayName("Remove Movie from List Operations")
    class RemoveMovieFromListTests {

        @Test
        @DisplayName("Should remove an existing movie from a list")
        void testRemoveExistingMovie() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");
            MovieModel movie2 = createMovie(102, "Interstellar");

            listMovieDao.addMovieToList(list1, movie1);
            listMovieDao.addMovieToList(list1, movie2);

            listMovieDao.removeMovieFromList(list1, movie1);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList);
            assertEquals(1, moviesInList.size(), "List should contain one movie after removal");
            assertFalse(moviesInList.contains(movie1), "Movie1 should have been removed");
            assertTrue(moviesInList.contains(movie2), "Movie2 should still be in the list");
        }

        @Test
        @DisplayName("Should make the list empty when the last movie is removed")
        void testRemoveLastMovieMakesListEmpty() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");

            listMovieDao.addMovieToList(list1, movie1);
            listMovieDao.removeMovieFromList(list1, movie1);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList);
            assertTrue(moviesInList.isEmpty(), "List should be empty after removing the last movie");
        }

        @Test
        @DisplayName("Should throw DaoException when removing a non-existent movie from an existing list")
        void testRemoveNonExistentMovieThrowsDaoException() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");
            MovieModel movieToRemove = createMovie(999, "Non Existent Movie"); // Not in the list

            listMovieDao.addMovieToList(list1, movie1);

            DaoException thrown = assertThrows(DaoException.class, () -> {
                listMovieDao.removeMovieFromList(list1, movieToRemove);
            }, "Expected DaoException for non-existent movie removal");

            assertTrue(thrown.getMessage().contains("not found"), "Exception message should indicate movie not found");

            // Verify the original movie is still there
            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertEquals(1, moviesInList.size(), "Original movie should still be in the list");
            assertTrue(moviesInList.contains(movie1), "Movie1 should still be in the list");
        }

        @Test
        @DisplayName("Should throw DaoException when removing from a non-existent list")
        void testRemoveMovieFromNonExistentListThrowsDaoException() {
            ListModel nonExistentList = createList(99, "Non Existent List", "userX");
            MovieModel movie1 = createMovie(101, "Inception");

            DaoException thrown = assertThrows(DaoException.class, () -> {
                listMovieDao.removeMovieFromList(nonExistentList, movie1);
            }, "Expected DaoException when removing from a non-existent list");

            assertTrue(thrown.getMessage().contains("not found"), "Exception message should indicate movie not found in list");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when removing a movie with null ListModel")
        void testRemoveMovieWithNullListThrowsIllegalArgumentException() {
            MovieModel movie1 = createMovie(101, "Inception");
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.removeMovieFromList(null, movie1),
                    "Expected IllegalArgumentException for null ListModel");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when removing a movie with null MovieModel")
        void testRemoveMovieWithNullMovieThrowsIllegalArgumentException() {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.removeMovieFromList(list1, null),
                    "Expected IllegalArgumentException for null MovieModel");
        }
    }

    // --- Tests for getAllMoviesInList ---
    @Nested
    @DisplayName("Get All Movies in List Operations")
    class GetAllMoviesInListTests {

        @Test
        @DisplayName("Should return an empty list for a new, empty list")
        void testGetAllMoviesInEmptyList() throws DaoException {
            ListModel list1 = createList(1, "Empty List", "user1");
            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList, "Returned list should not be null");
            assertTrue(moviesInList.isEmpty(), "Returned list should be empty");
        }

        @Test
        @DisplayName("Should return an empty list for a non-existent list ID")
        void testGetAllMoviesInNonExistentList() throws DaoException {
            ListModel nonExistentList = createList(99, "Non Existent", "userX");
            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(nonExistentList);
            assertNotNull(moviesInList);
            assertTrue(moviesInList.isEmpty(), "Returned list should be empty for a non-existent list");
        }

        @Test
        @DisplayName("Should return all movies correctly from a populated list")
        void testGetAllMoviesInPopulatedList() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");
            MovieModel movie2 = createMovie(102, "Interstellar");

            listMovieDao.addMovieToList(list1, movie1);
            listMovieDao.addMovieToList(list1, movie2);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList);
            assertEquals(2, moviesInList.size(), "Returned list should have 2 movies");
            assertTrue(moviesInList.contains(movie1), "Returned list should contain movie1");
            assertTrue(moviesInList.contains(movie2), "Returned list should contain movie2");
        }

        @Test
        @DisplayName("Should return an unmodifiable list view")
        void testReturnedListIsUnmodifiable() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");

            listMovieDao.addMovieToList(list1, movie1);

            List<MovieModel> movies = listMovieDao.getAllMoviesInList(list1);

            // Attempt to modify the returned list
            MovieModel newMovie = createMovie(999, "New Movie");
            assertThrows(UnsupportedOperationException.class, () -> movies.add(newMovie),
                    "Adding to the returned list should throw UnsupportedOperationException");
            assertThrows(UnsupportedOperationException.class, () -> movies.remove(0),
                    "Removing from the returned list should throw UnsupportedOperationException");
            assertThrows(UnsupportedOperationException.class, movies::clear, // Refactored here
                    "Clearing the returned list should throw UnsupportedOperationException");

            // Verify the original list in the DAO is unaffected by modification attempts
            List<MovieModel> originalListState = listMovieDao.getAllMoviesInList(list1);
            assertEquals(1, originalListState.size(), "Original list size should remain 1");
            assertTrue(originalListState.contains(movie1), "Original list should still contain movie1");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when getting movies with null ListModel")
        void testGetAllMoviesWithNullListThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.getAllMoviesInList(null),
                    "Expected IllegalArgumentException for null ListModel");
        }
    }

    // --- Tests for removeAllMoviesFromList ---
    @Nested
    @DisplayName("Remove All Movies from List Operations")
    class RemoveAllMoviesFromListTests {

        @Test
        @DisplayName("Should remove all movies from a populated list")
        void testRemoveAllMoviesFromList() throws DaoException {
            ListModel list1 = createList(1, "My Watchlist", "user1");
            MovieModel movie1 = createMovie(101, "Inception");
            MovieModel movie2 = createMovie(102, "Interstellar");

            listMovieDao.addMovieToList(list1, movie1);
            listMovieDao.addMovieToList(list1, movie2);

            listMovieDao.removeAllMoviesFromList(list1);

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertNotNull(moviesInList);
            assertTrue(moviesInList.isEmpty(), "List should be empty after removing all movies");
        }

        @Test
        @DisplayName("Should handle removing all movies from an already empty list gracefully")
        void testRemoveAllMoviesFromEmptyList() throws DaoException {
            ListModel list1 = createList(1, "Empty List", "user1");
            // No movies added to list1

            assertDoesNotThrow(() -> listMovieDao.removeAllMoviesFromList(list1),
                    "Should not throw exception when removing all movies from an empty list");

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(list1);
            assertTrue(moviesInList.isEmpty(), "List should remain empty");
        }

        @Test
        @DisplayName("Should handle removing all movies from a non-existent list gracefully")
        void testRemoveAllMoviesFromNonExistentList() throws DaoException {
            ListModel nonExistentList = createList(99, "Non Existent List", "userX");

            assertDoesNotThrow(() -> listMovieDao.removeAllMoviesFromList(nonExistentList),
                    "Should not throw exception when removing all movies from a non-existent list");

            List<MovieModel> moviesInList = listMovieDao.getAllMoviesInList(nonExistentList);
            assertTrue(moviesInList.isEmpty(), "Non-existent list should still appear empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when removing all movies with null ListModel")
        void testRemoveAllMoviesWithNullListThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> listMovieDao.removeAllMoviesFromList(null),
                    "Expected IllegalArgumentException for null ListModel");
        }
    }

    // --- General/Integration Tests ---
    @Test
    @DisplayName("Test isolation between different lists")
    void testIsolationBetweenLists() throws DaoException {
        ListModel list1 = createList(1, "List A", "user1");
        ListModel list2 = createList(2, "List B", "user2");
        MovieModel movie1 = createMovie(101, "Movie X");
        MovieModel movie2 = createMovie(102, "Movie Y");
        MovieModel movie3 = createMovie(103, "Movie Z");

        listMovieDao.addMovieToList(list1, movie1);
        listMovieDao.addMovieToList(list1, movie2);
        listMovieDao.addMovieToList(list2, movie3);

        List<MovieModel> moviesInList1 = listMovieDao.getAllMoviesInList(list1);
        assertEquals(2, moviesInList1.size(), "List A should have 2 movies");
        assertTrue(moviesInList1.contains(movie1), "List A should contain Movie X");
        assertTrue(moviesInList1.contains(movie2), "List A should contain Movie Y");
        assertFalse(moviesInList1.contains(movie3), "List A should NOT contain Movie Z");

        List<MovieModel> moviesInList2 = listMovieDao.getAllMoviesInList(list2);
        assertEquals(1, moviesInList2.size(), "List B should have 1 movie");
        assertTrue(moviesInList2.contains(movie3), "List B should contain Movie Z");
        assertFalse(moviesInList2.contains(movie1), "List B should NOT contain Movie X");
        assertFalse(moviesInList2.contains(movie2), "List B should NOT contain Movie Y");
    }
}