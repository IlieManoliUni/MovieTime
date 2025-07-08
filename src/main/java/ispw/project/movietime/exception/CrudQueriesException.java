package ispw.project.movietime.exception;

public class CrudQueriesException extends Exception {
    public CrudQueriesException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrudQueriesException(String message) {
        super(message);
    }
}
