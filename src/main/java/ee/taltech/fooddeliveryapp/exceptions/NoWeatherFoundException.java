package ee.taltech.fooddeliveryapp.exceptions;

/**
 * Thrown when the database contains no valid weather data.
 */
public class NoWeatherFoundException extends Throwable {
    private final String message;
    private final Throwable cause;

    public NoWeatherFoundException(String message) {
        this(message, null);
    }

    public NoWeatherFoundException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
