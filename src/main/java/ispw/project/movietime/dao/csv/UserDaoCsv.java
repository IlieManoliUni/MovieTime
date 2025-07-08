package ispw.project.movietime.dao.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.model.UserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDaoCsv implements UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDaoCsv.class.getName());

    private static final String CSV_FILE_NAME;

    private final HashMap<String, UserModel> localCache;

    static {
        Properties properties = new Properties();
        String fileName = "user.csv";

        try (InputStream input = UserDaoCsv.class.getClassLoader().getResourceAsStream("csv.properties")) {
            if (input != null) {
                properties.load(input);
                fileName = properties.getProperty("FILE_USER", fileName);
            } else {
                LOGGER.log(Level.WARNING, "csv.properties file not found. Using default CSV filename for users: ''{0}''", fileName);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Error loading csv.properties.", e);
        }
        CSV_FILE_NAME = fileName;

        try {
            if (!Files.exists(Paths.get(CSV_FILE_NAME))) {
                Files.createFile(Paths.get(CSV_FILE_NAME));
                LOGGER.log(Level.INFO, "Created new CSV file for users: {0}", CSV_FILE_NAME);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Could not create CSV file for users.", e);
        }
    }

    public UserDaoCsv() {
        this.localCache = new HashMap<>();
    }

    @Override
    public UserModel retrieveByUsername(String username) throws CsvException {
        synchronized (localCache) {
            if (localCache.containsKey(username)) {
                return localCache.get(username);
            }
        }

        UserModel user = null;
        try {
            user = retrieveByUsernameFromFile(username);
        } catch (IOException e) {
            throw new CsvException("Failed to retrieve user from CSV for username: " + username + ". I/O error.", e);
        } catch (CsvValidationException e) {
            throw new CsvException("CSV data validation error while retrieving user for username: " + username, e);
        }

        if (user != null) {
            synchronized (localCache) {
                localCache.put(username, user);
            }
        }
        return user;
    }

    private UserModel retrieveByUsernameFromFile(String username) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordUser;
            while ((recordUser = csvReader.readNext()) != null) {
                if (recordUser.length < 2) {
                    LOGGER.log(Level.WARNING, "Skipping malformed user record: not enough columns. Record: {0}", java.util.Arrays.toString(recordUser));
                    continue;
                }
                if (recordUser[0].equals(username)) {
                    return new UserModel(recordUser[0], recordUser[1]);
                }
            }
        }
        return null;
    }

    @Override
    public void saveUser(UserModel user) throws CsvException {
        String username = user.getUsername();

        synchronized (localCache) {
            if (localCache.containsKey(username)) {
                LOGGER.log(Level.WARNING, "User with username ''{0}'' already in cache. Cannot save as new.", username);
                throw new CsvException("User with username '" + username + "' already in cache.");
            }
        }

        UserModel existingUser = null;
        try {
            existingUser = retrieveByUsernameFromFile(username);
        } catch (IOException e) {
            throw new CsvException("Failed to check existing user for username: " + username + ". I/O error.", e);
        } catch (CsvValidationException e) {
            throw new CsvException("CSV data validation error while checking existing user for username: " + username, e);
        }

        if (existingUser != null) {
            LOGGER.log(Level.WARNING, "Duplicated Username: ''{0}''. User already exists in CSV file. Cannot save as new.", username);
            throw new CsvException("Duplicated Username: " + username + ". User already exists in CSV file.");
        }

        try {
            saveUserToFile(user);
            LOGGER.log(Level.INFO, "Successfully saved new user ''{0}'' to CSV.", user.getUsername());
        } catch (IOException e) {
            throw new CsvException("Failed to save user to CSV for username: " + username + ". I/O error.", e);
        }

        synchronized (localCache) {
            localCache.put(username, user);
        }
    }

    private void saveUserToFile(UserModel user) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(Paths.get(CSV_FILE_NAME), StandardOpenOption.APPEND))) {
            String[] recordUser = {user.getUsername(), user.getPassword()};
            csvWriter.writeNext(recordUser);
        }
    }
}