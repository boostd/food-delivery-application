package ee.taltech.fooddeliveryapp.exceptions;

/**
 * Thrown when no valid weather data was found for the selected timestamp.
 */
public class InvalidTimeStampException extends Throwable {
    private final String message;
    private final Throwable cause;

    public InvalidTimeStampException(String message) {
        this(message, null);
    }

    public InvalidTimeStampException(String message, Throwable cause) {
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
