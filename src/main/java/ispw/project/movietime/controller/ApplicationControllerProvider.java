package ispw.project.movietime.controller;

import ispw.project.movietime.dao.DaoType;
import ispw.project.movietime.dao.*;
import ispw.project.movietime.session.SessionManager;
import ispw.project.movietime.state.DemoModeState;
import ispw.project.movietime.state.FullModeState;
import ispw.project.movietime.state.PersistenceModeState;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationControllerProvider {

    private static final Logger LOGGER = Logger.getLogger(ApplicationControllerProvider.class.getName());
    private static ApplicationControllerProvider instance;

    private final UserDao userDao;
    private final ListDao listDao;
    private final MovieDao movieDao;
    private final ListMovie listMovie;
    private final SessionManager sessionManager;

    private ApplicationControllerProvider(UserDao userDao, ListDao listDao, MovieDao movieDao, ListMovie listMovie) {
        this.userDao = userDao;
        this.listDao = listDao;
        this.movieDao = movieDao;
        this.listMovie = listMovie;
        this.sessionManager = SessionManager.getInstance();

        if (userDao == null || listDao == null || movieDao == null || listMovie == null) {
            LOGGER.log(Level.SEVERE, "One or more DAOs provided to ApplicationControllerProvider are null.");
            throw new IllegalArgumentException("DAOs cannot be null.");
        }
    }

    public static synchronized void initialize(DaoType type) {
        if (instance != null) {
            LOGGER.log(Level.WARNING, "ApplicationControllerProvider already initialized. Ignoring subsequent call.");
            return;
        }

        LOGGER.log(Level.INFO, "Initializing ApplicationControllerProvider with persistence type: {0}", type);
        PersistenceModeState persistenceModeState;

        switch (type) {
            case INMEMORY:
                persistenceModeState = new DemoModeState();
                break;
            case JDBC:
                persistenceModeState = new FullModeState(DaoType.JDBC);
                break;
            case CSV:
                persistenceModeState = new FullModeState(DaoType.CSV);
                break;
            default:
                throw new IllegalArgumentException("Unsupported PersistenceType: " + type);
        }

        instance = new ApplicationControllerProvider(
                persistenceModeState.getUserDao(),
                persistenceModeState.getListDao(),
                persistenceModeState.getMovieDao(),
                persistenceModeState.getListMovieDao()
        );
    }

    public static synchronized ApplicationControllerProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApplicationControllerProvider not initialized. Call initialize() first.");
        }
        return instance;
    }

    public SessionManager getSessionManager() { return sessionManager; }
    public UserDao getUserDao() { return userDao; }
    public ListDao getListDao() { return listDao; }
    public MovieDao getMovieDao() { return movieDao; }
    public ListMovie getListMovieDao() { return listMovie; }
}