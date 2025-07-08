package ispw.project.movietime.exception;

public class CliCommandException extends Exception {

    public CliCommandException(String message) {
        super(message);
    }

    public CliCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CliCommandException(Throwable cause) {
        super(cause);
    }
}
