package ispw.project.movietime.dao.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.exception.CsvException;
import ispw.project.movietime.exception.DaoException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.model.UserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListDaoCsv implements ListDao {

    private static final Logger LOGGER = Logger.getLogger(ListDaoCsv.class.getName());

    private static final String CSV_FILE_NAME;

    private final HashMap<Integer, ListModel> localCache;

    private final ListMovie listMovieDao;

    static {
        Properties properties = new Properties();
        String fileName = "list.csv";

        try (InputStream input = ListDaoCsv.class.getClassLoader().getResourceAsStream("csv.properties")) {
            if (input != null) {
                properties.load(input);
                fileName = properties.getProperty("FILE_LIST", fileName);
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
                LOGGER.log(Level.INFO, "Created new CSV file for lists: {0}", CSV_FILE_NAME);
            }
        } catch (IOException e) {
            throw new CsvException("Initialization failed: Could not create CSV file for lists.", e);
        }
    }

    public ListDaoCsv() {
        this.localCache = new HashMap<>();
        this.listMovieDao = new ListMovieDaoCsv();
    }

    @Override
    public ListModel retrieveById(int id) throws CsvException, DaoException, CrudQueriesException {
        synchronized (localCache) {
            if (localCache.containsKey(id)) {
                return localCache.get(id);
            }
        }

        ListModel list = null;
        try {
            list = retrieveByIdFromFile(id);
            if (list != null) {
                List<MovieModel> movies = listMovieDao.getAllMoviesInList(list);
                list.setMovies(movies); // Populate the movies in the ListModel
                LOGGER.log(Level.INFO, "Loaded {0} movies for list ''{1}'' (ID: {2}).", new Object[]{movies.size(), list.getName(), list.getId()});
            }
        } catch (IOException | NumberFormatException e) {
            throw new CsvException("Failed to retrieve list from CSV for ID: " + id + ". Data corruption or I/O error.", e);
        } catch (CsvException e) {
            throw e;
        } catch (RuntimeException | DaoException e) { // Combines both catch types
            throw new DaoException("An unexpected system error occurred during DAO operation.", e);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        }

        if (list != null) {
            synchronized (localCache) {
                localCache.put(id, list);
            }
        }
        return list;
    }

    private ListModel retrieveByIdFromFile(int id) throws IOException, NumberFormatException, CsvException {
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordList;
            while ((recordList = csvReader.readNext()) != null) {
                ListModel foundList = findListRecordById(recordList, id);
                if (foundList != null) {
                    return foundList;
                }
            }
        } catch (CsvValidationException e) {
            throw new CsvException("CSV validation error during retrieveByIdFromFile.", e);
        }
        return null;
    }

    private ListModel findListRecordById(String[] recordList, int targetId) throws CsvException, NumberFormatException {
        if (recordList.length < 1) {
            return null;
        }
        int currentId = 0;
        try {
            currentId = Integer.parseInt(recordList[0]);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Skipping malformed record due to invalid ID format. Record: {0}, Error: {1}",
                    new Object[]{Arrays.toString(recordList), e.getMessage()});
        }

        if (currentId == targetId) {
            if (recordList.length < 3) {
                LOGGER.log(Level.WARNING, "Malformed record found for ID {0}. Expected 3 columns, found {1}. Record: {2}",
                        new Object[]{targetId, recordList.length, Arrays.toString(recordList)});
                throw new CsvException("Malformed record for ID " + targetId + ": not enough columns to create ListBean.");
            }
            return new ListModel(currentId, recordList[1], recordList[2]);
        }
        return null;
    }

    private int parseRecordIdForGeneration(String[] recordList) {
        if (recordList.length > 0) {
            try {
                return Integer.parseInt(recordList[0]);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Skipping malformed record during ID generation due to invalid ID format: {0}, Error: {1}",
                        new Object[]{Arrays.toString(recordList), e.getMessage()});
                return 0;
            }
        }
        return 0;
    }

    private int generateNewListId() throws IOException, CsvException {
        int maxId = 0;
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordList;
            while ((recordList = csvReader.readNext()) != null) {
                int currentId = parseRecordIdForGeneration(recordList);
                if (currentId > maxId) {
                    maxId = currentId;
                }
            }
        } catch (CsvValidationException e) {
            throw new CsvException("CSV validation error during ID generation.", e);
        }
        return maxId + 1;
    }

    @Override
    public void saveList(ListModel list, UserModel user) throws CsvException {
        int listId = list.getId();
        if (listId == 0) {
            try {
                listId = generateNewListId();
                list.setId(listId);
            } catch (IOException | CsvException e) {
                throw new CsvException("Failed to generate a new ID for the list.", e);
            }
        }

        synchronized (localCache) {
            if (localCache.containsKey(listId)) {
                LOGGER.log(Level.WARNING, "List with ID {0} already in cache. Cannot save as new.", listId);
                throw new CsvException("List with ID " + listId + " already in cache.");
            }
        }

        ListModel existingList = null;
        try {
            existingList = retrieveByIdFromFile(listId);
        } catch (IOException | NumberFormatException e) {
            throw new CsvException("Failed to check existing list for ID: " + listId + ". Data corruption or I/O error.", e);
        } catch (CsvException e) {
            throw new CsvException("CSV data validation error while checking existing list for ID: " + listId, e);
        }

        if (existingList != null) {
            LOGGER.log(Level.WARNING, "List with ID {0} already exists in CSV file. Cannot save as new.", listId);
            throw new CsvException("List with ID " + listId + " already exists in CSV file.");
        }

        try {
            saveListToFile(list);
            LOGGER.log(Level.INFO, "Successfully saved new list ''{0}'' with ID {1} to CSV.", new Object[]{list.getName(), list.getId()});
        } catch (IOException e) {
            throw new CsvException("Failed to save list to CSV for ID: " + listId + ". I/O error.", e);
        }

        synchronized (localCache) {
            localCache.put(listId, list);
        }
    }

    private void saveListToFile(ListModel list) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(Paths.get(CSV_FILE_NAME), StandardOpenOption.APPEND))) {
            String[] recordList = {
                    String.valueOf(list.getId()),
                    list.getName(),
                    list.getUsername()
            };
            csvWriter.writeNext(recordList);
        }
    }

    @Override
    public void deleteList(ListModel list) throws CsvException, DaoException, CrudQueriesException {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null for deletion.");
        }
        synchronized (localCache) {
            localCache.remove(list.getId());
        }

        try {
            deleteListFromFile(list);
            listMovieDao.removeAllMoviesFromList(list);
            LOGGER.log(Level.INFO, "Successfully deleted list ''{0}'' with ID {1} from CSV and removed associated movies.", new Object[]{list.getName(), list.getId()});
        } catch (IOException | CsvException e) {
            throw new CsvException("Failed to delete list from CSV or remove associated movies. I/O or data error.", e);
        } catch (RuntimeException | DaoException e) { // Combines both catch types
            throw new DaoException("An unexpected system error occurred during DAO operation.", e);
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
        }
    }

    private void deleteListFromFile(ListModel list) throws IOException, CsvException {
        Path originalPath = Paths.get(CSV_FILE_NAME);
        Path tempPath = Paths.get(CSV_FILE_NAME + ".tmp");

        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(originalPath));
             CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(tempPath))) {

            String[] recordList;
            while ((recordList = csvReader.readNext()) != null) {
                if (recordList.length < 1) {
                    csvWriter.writeNext(recordList);
                    continue;
                }
                processListRecordForDeletion(recordList, list.getId(), csvWriter);
            }
        } catch (CsvValidationException e) {
            throw new CsvException("CSV validation error during deleteListFromFile.", e);
        }

        Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void processListRecordForDeletion(String[] recordList, int listIdToDelete, CSVWriter csvWriter) {
        try {
            if (recordList[0] != null && !recordList[0].trim().isEmpty() && Integer.parseInt(recordList[0]) != listIdToDelete) {
                csvWriter.writeNext(recordList);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Skipping removal for malformed list record in CSV due to invalid ID format. Record will be preserved. Record: {0}, Error: {1}",
                    new Object[]{Arrays.toString(recordList), e.getMessage()});
            csvWriter.writeNext(recordList);
        }
    }

    @Override
    public List<ListModel> retrieveAllListsOfUsername(String username) throws CsvException, DaoException, CrudQueriesException {
        List<ListModel> allLists = null;
        try {
            allLists = retrieveAllListsFromFile();
        } catch (IOException | NumberFormatException e) {
            throw new CsvException("Failed to retrieve all lists from CSV. Data corruption or I/O error.", e);
        } catch (CsvException e) {
            throw new CsvException("CSV data validation error while retrieving all lists.", e);
        }

        List<ListModel> userLists = new ArrayList<>();
        for (ListModel list : allLists) {
            if (list.getUsername().equals(username)) {
                try {
                    List<MovieModel> movies = listMovieDao.getAllMoviesInList(list);
                    list.setMovies(movies);
                } catch (CsvException e) {
                    LOGGER.log(Level.WARNING, "Failed to load movies for list ''{0}'' (ID: {1}) of user ''{2}''. Skipping movies for this list. Error: {3}",
                            new Object[]{list.getName(), list.getId(), username, e.getMessage()});
                } catch (RuntimeException | DaoException e) {
                    throw new DaoException("An unexpected system error occurred during DAO operation.", e);
                } catch (CrudQueriesException e) {
                    throw new CrudQueriesException("Failed to perform a CRUD operation in [YourSpecificMethodName].", e);
                }
                userLists.add(list);
            }
        }

        synchronized (localCache) {
            localCache.clear();
            for (ListModel list : allLists) {
                localCache.put(list.getId(), list);
            }
        }

        LOGGER.log(Level.INFO, "Retrieved {0} lists for user ''{1}''.", new Object[]{userLists.size(), username});
        return Collections.unmodifiableList(userLists);
    }

    private List<ListModel> retrieveAllListsFromFile() throws IOException, NumberFormatException, CsvException {
        List<ListModel> listModels = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(Paths.get(CSV_FILE_NAME)))) {
            String[] recordList;
            while ((recordList = csvReader.readNext()) != null) {
                if (recordList.length < 3) {
                    LOGGER.log(Level.WARNING, "Skipping malformed record in CSV during retrieveAllListsFromFile: not enough columns. Record: {0}", Arrays.toString(recordList));
                    continue;
                }
                ListModel parsedList = parseListRecord(recordList);
                if (parsedList != null) {
                    listModels.add(parsedList);
                }
            }
        } catch (CsvValidationException e) {
            throw new CsvException("CSV validation error during retrieveAllListsFromFile.", e);
        }
        return listModels;
    }

    private ListModel parseListRecord(String[] recordList) {
        try {
            return new ListModel(Integer.parseInt(recordList[0]), recordList[1], recordList[2]);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Skipping malformed list record in CSV. Expected numeric ID, but found invalid data. Record: {0}, Error: {1}",
                    new Object[]{Arrays.toString(recordList), e.getMessage()});
            return null;
        }
    }
}