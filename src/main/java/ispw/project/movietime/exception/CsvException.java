package ispw.project.movietime.exception;

public class CsvException extends RuntimeException {

    public CsvException(String message) {
        super(message);
    }

    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvException(Throwable cause) {
        super(cause);
    }
}
