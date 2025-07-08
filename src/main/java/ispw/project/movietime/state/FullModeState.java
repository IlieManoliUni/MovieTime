package ispw.project.movietime.state;

import ispw.project.movietime.dao.*;
import ispw.project.movietime.dao.csv.*;
import ispw.project.movietime.dao.jdbc.*;

public class FullModeState implements PersistenceModeState {

    private final UserDao userDao;
    private final ListDao listDao;
    private final MovieDao movieDao;
    private final ListMovie listMovieDao;

    public FullModeState(DaoType daoType) {

        switch (daoType) {
            case JDBC:
                try {
                    this.userDao = new UserDaoJdbc();
                    this.listDao = new ListDaoJdbc();
                    this.movieDao = new MovieDaoJdbc();
                    this.listMovieDao = new ListMovieDaoJdbc();
                } catch (Exception e) {
                    throw new IllegalStateException("Error initializing JDBC DAOs.", e);
                }
                break;

            case CSV:
                try {
                    this.userDao = new UserDaoCsv();
                    this.listDao = new ListDaoCsv();
                    this.movieDao = new MovieDaoCsv();
                    this.listMovieDao = new ListMovieDaoCsv();
                } catch (Exception e) {
                    throw new IllegalStateException("Error initializing CSV DAOs.", e);
                }
                break;

            default:
                throw new IllegalStateException("Invalid DaoType for FullModeState. Must be JDBC or CSV.");
        }
    }

    @Override
    public UserDao getUserDao() {
        return userDao;
    }

    @Override
    public ListDao getListDao() {
        return listDao;
    }

    @Override
    public MovieDao getMovieDao() {
        return movieDao;
    }

    @Override
    public ListMovie getListMovieDao() {
        return listMovieDao;
    }

}