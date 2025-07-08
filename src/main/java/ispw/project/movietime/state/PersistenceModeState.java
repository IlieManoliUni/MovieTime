package ispw.project.movietime.state;

import ispw.project.movietime.dao.*;

public interface PersistenceModeState {
    UserDao getUserDao();
    ListDao getListDao();
    MovieDao getMovieDao();
    ListMovie getListMovieDao();
}