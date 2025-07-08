package ispw.project.movietime.state;


import ispw.project.movietime.dao.ListDao;
import ispw.project.movietime.dao.ListMovie;
import ispw.project.movietime.dao.MovieDao;
import ispw.project.movietime.dao.UserDao;
import ispw.project.movietime.dao.memory.ListDaoInMemory;
import ispw.project.movietime.dao.memory.ListMovieDaoInMemory;
import ispw.project.movietime.dao.memory.MovieDaoInMemory;
import ispw.project.movietime.dao.memory.UserDaoInMemory;

public class DemoModeState implements PersistenceModeState {

    private final UserDao userDao;
    private final ListDao listDao;
    private final MovieDao movieDao;
    private final ListMovie listMovieDao;

    public DemoModeState() {
        this.userDao = new UserDaoInMemory();
        this.listDao = new ListDaoInMemory();
        this.movieDao = new MovieDaoInMemory();
        this.listMovieDao = new ListMovieDaoInMemory();
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

    @Override // New method
    public ListMovie getListMovieDao() {
        return listMovieDao;
    }

}