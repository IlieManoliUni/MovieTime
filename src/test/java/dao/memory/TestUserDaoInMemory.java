package dao.memory;

import ispw.project.movietime.dao.memory.UserDaoInMemory;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserDaoInMemory {

    private UserDaoInMemory userDaoInMemory;
    private Map<String, UserModel> usersMap; // To access the private 'users' map via reflection

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        userDaoInMemory = new UserDaoInMemory();

        // Use reflection to access and clear the private 'users' map before each test
        // This is crucial to ensure test isolation since the map is declared as 'final'
        Field usersField = UserDaoInMemory.class.getDeclaredField("users");
        usersField.setAccessible(true); // Allow access to private field
        usersMap = (Map<String, UserModel>) usersField.get(userDaoInMemory);
        usersMap.clear(); // Clear the map to ensure a clean state for each test
    }

    // --- retrieveByUsername Tests ---

    @Test
    void testRetrieveByUsernameExistingUser() throws DaoException {
        // Arrange
        UserModel existingUser = new UserModel("testUser", "password123");
        usersMap.put(existingUser.getUsername(), existingUser);

        // Act
        UserModel retrievedUser = userDaoInMemory.retrieveByUsername("testUser");

        // Assert
        assertNotNull(retrievedUser, "User should be retrieved successfully.");
        assertEquals(existingUser.getUsername(), retrievedUser.getUsername(), "Retrieved username should match.");
        assertEquals(existingUser.getPassword(), retrievedUser.getPassword(), "Retrieved password should match.");
    }

    @Test
    void testRetrieveByUsernameNonExistentUser() throws DaoException {
        // Act
        UserModel retrievedUser = userDaoInMemory.retrieveByUsername("nonExistentUser");

        // Assert
        assertNull(retrievedUser, "Should return null for a non-existent user.");
    }

    @Test
    void testRetrieveByUsernameNullThrowsIllegalArgumentException() {
        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.retrieveByUsername(null));
        assertEquals("Username cannot be null or empty.", thrown.getMessage());
    }

    @Test
    void testRetrieveByUsernameEmptyThrowsIllegalArgumentException() {
        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.retrieveByUsername(""));
        assertEquals("Username cannot be null or empty.", thrown.getMessage());
    }

    @Test
    void testRetrieveByUsernameBlankThrowsIllegalArgumentException() {
        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.retrieveByUsername("   "));
        assertEquals("Username cannot be null or empty.", thrown.getMessage());
    }

    // --- saveUser Tests ---

    @Test
    void testSaveUserSuccessfully() throws DaoException {
        // Arrange
        UserModel newUser = new UserModel("newUser", "newPass");

        // Act
        userDaoInMemory.saveUser(newUser);

        // Assert
        assertTrue(usersMap.containsKey("newUser"), "User should be added to the map.");
        assertEquals(newUser, usersMap.get("newUser"), "Stored user object should be the same.");
    }

    @Test
    void testSaveUserExistingUsernameThrowsDaoException() throws DaoException {
        // Arrange
        UserModel existingUser = new UserModel("existingUser", "oldPass");
        usersMap.put(existingUser.getUsername(), existingUser);

        UserModel userWithSameUsername = new UserModel("existingUser", "newPass"); // Different password, same username

        // Act & Assert
        DaoException thrown = assertThrows(DaoException.class,
                () -> userDaoInMemory.saveUser(userWithSameUsername));
        assertEquals("Username already exists: existingUser", thrown.getMessage(), "Should throw DaoException for existing username.");
        // Ensure the original user object is still in the map (no update happened)
        assertEquals("oldPass", usersMap.get("existingUser").getPassword(), "Existing user's data should not be overwritten.");
    }

    @Test
    void testSaveUserNullUserThrowsIllegalArgumentException() {
        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.saveUser(null));
        assertEquals("User model cannot be null.", thrown.getMessage());
    }

    @Test
    void testSaveUserNullUsernameThrowsIllegalArgumentException() {
        // Arrange
        UserModel user = new UserModel(null, "password");

        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.saveUser(user));
        assertEquals("User's username cannot be null or empty.", thrown.getMessage());
    }

    @Test
    void testSaveUserEmptyUsernameThrowsIllegalArgumentException() {
        // Arrange
        UserModel user = new UserModel("", "password");

        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.saveUser(user));
        assertEquals("User's username cannot be null or empty.", thrown.getMessage());
    }

    @Test
    void testSaveUserBlankUsernameThrowsIllegalArgumentException() {
        // Arrange
        UserModel user = new UserModel("   ", "password");

        // Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userDaoInMemory.saveUser(user));
        assertEquals("User's username cannot be null or empty.", thrown.getMessage());
    }
}