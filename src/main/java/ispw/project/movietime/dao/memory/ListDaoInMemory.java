package ispw.project.movietime.dao.memory;

import ispw.project.movietime.exception.CrudQueriesException;
import ispw.project.movietime.model.ListModel;
import ispw.project.movietime.model.UserModel;
import ispw.project.movietime.model.MovieModel;
import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.exception.DaoException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ListDaoInMemory implements ListDao {

    private static final Logger LOGGER = Logger.getLogger(ListDaoInMemory.class.getName());

    private final Map<Integer, ListModel> listMap = new HashMap<>();
    private final Map<String, Set<Integer>> userListsMap = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    private final ListMovie listMovieDao;

    public ListDaoInMemory() {
        this.listMovieDao = new ListMovieDaoInMemory();

    }


    @Override
    public ListModel retrieveById(int id) throws DaoException, CrudQueriesException {
        ListModel list = listMap.get(id);
        if (list != null) {
            try {
                List<MovieModel> movies = listMovieDao.getAllMoviesInList(list);
                list.setMovies(movies);
                LOGGER.log(Level.INFO, "Loaded {0} movies for list ID {1}.", new Object[]{movies.size(), id});
            } catch (DaoException e) {
                LOGGER.log(Level.WARNING, "Failed to load movies for list ID {0} from ListMovieDaoInMemory. Error: {1}", new Object[]{id, e.getMessage()});
            } catch (CrudQueriesException e) {
                throw new CrudQueriesException("Failed to perform a CRUD operation in retrieveById.", e);
            }
        }
        return list;
    }

    @Override
    public void saveList(ListModel list, UserModel user) throws DaoException {
        if (list == null || user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("ListModel, UserModel, or UserModel's username cannot be null.");
        }

        int listId = list.getId();
        String username = user.getUsername();

        if (listId == 0) {
            listId = idGenerator.incrementAndGet();
            list.setId(listId);
            LOGGER.log(Level.INFO, "Generated new ID {0} for list ''{1}''.", new Object[]{listId, list.getName()});
        }

        if (listMap.containsKey(listId)) {
            throw new DaoException("List with ID " + listId + " already exists. This ID was either manually set to an existing ID, or ID generation failed.");
        }

        if (!Objects.equals(list.getUsername(), username)) {
            LOGGER.log(Level.WARNING, "ListModel''s username ({0}) does not match UserModel''s username ({1}). Setting ListModel''s username.", new Object[]{list.getUsername(), username});
            list.setUsername(username);
        }

        listMap.put(listId, list);
        userListsMap.computeIfAbsent(username, k -> new HashSet<>()).add(listId);

        LOGGER.log(Level.INFO, "List with ID {0} saved for user {1}.", new Object[]{listId, username});
    }

    @Override
    public void deleteList(ListModel list) throws DaoException, CrudQueriesException {
        if (list == null) {
            throw new IllegalArgumentException("ListModel cannot be null for deletion.");
        }

        int listId = list.getId();
        String listOwnerUsername = list.getUsername();

        if (listOwnerUsername == null) {
            throw new IllegalArgumentException("Cannot delete list: ListModel's username is null. Ensure the ListModel is fully populated before attempting deletion.");
        }

        if (!listMap.containsKey(listId)) {
            throw new DaoException("List with ID " + listId + " not found for deletion. No action taken.");
        }

        ListModel storedList = listMap.get(listId);
        if (!Objects.equals(storedList.getUsername(), listOwnerUsername)) {
            LOGGER.log(Level.WARNING, "Attempt to delete list {0} by user {1}, but it''s owned by {2}. Deletion denied.",
                    new Object[]{listId, listOwnerUsername, storedList.getUsername()});
            throw new DaoException("User " + listOwnerUsername + " does not own list with ID " + listId + ". Deletion failed.");
        }

        listMap.remove(listId);

        Set<Integer> userLists = userListsMap.get(listOwnerUsername);
        if (userLists != null) {
            userLists.remove(listId);
            if (userLists.isEmpty()) {
                userListsMap.remove(listOwnerUsername);
                LOGGER.log(Level.INFO, "User {0} no longer has any lists mapped, removing user entry from userListsMap.", listOwnerUsername);
            }
        }
        LOGGER.log(Level.INFO, "List with ID {0} deleted for user {1}.", new Object[]{listId, listOwnerUsername});

        try {
            listMovieDao.removeAllMoviesFromList(list);
            LOGGER.log(Level.INFO, "Removed all movie associations for deleted list ID {0}.", listId);
        } catch (DaoException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove movie associations for deleted list ID {0}: {1}", new Object[]{listId, e.getMessage()});
        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation in deleteList.", e);
        }
    }

    @Override
    public List<ListModel> retrieveAllListsOfUsername(String username) throws DaoException, CrudQueriesException {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null for retrieving lists.");
        }

        List<ListModel> result = new ArrayList<>();
        Set<Integer> ids = userListsMap.get(username);

        if (ids != null) {
            for (int id : ids) {
                ListModel list = listMap.get(id);
                if (list == null) {
                    LOGGER.log(Level.WARNING, "List ID {0} found for user {1} in userListsMap but not in main listMap. Data inconsistency detected.", new Object[]{id, username});
                    continue;
                }

                processAndAddList(list, username, result);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private void processAndAddList(ListModel list, String username, List<ListModel> result) throws CrudQueriesException {
        try {
            List<MovieModel> movies = listMovieDao.getAllMoviesInList(list);
            list.setMovies(movies);
            LOGGER.log(Level.INFO, "Loaded {0} movies for list ID {1} (user {2}).", new Object[]{movies.size(), list.getId(), username});
        } catch (DaoException e) {
            LOGGER.log(Level.WARNING, "Failed to load movies for list ID {0} (user {1}) from ListMovieDaoInMemory. Error: {2}", new Object[]{list.getId(), username, e.getMessage()});

        } catch (CrudQueriesException e) {
            throw new CrudQueriesException("Failed to perform a CRUD operation during movie loading for list ID " + list.getId() + ".", e);
        }
        result.add(list);
    }
}