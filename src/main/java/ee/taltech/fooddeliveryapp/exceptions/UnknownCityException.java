package ee.taltech.fooddeliveryapp.exceptions;

public class UnknownCityException extends Throwable {
    private final String message;
    private final Throwable cause;

    public UnknownCityException(String message) {
        this(message, null);
    }

    public UnknownCityException(String message, Throwable cause) {
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
