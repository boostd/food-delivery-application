package ee.taltech.fooddeliveryapp.service;

public class UnknownCityException extends Throwable {
    private final String message;
    private final Throwable cause;

    UnknownCityException(String message) {
        this(message, null);
    }

    UnknownCityException(String message, Throwable cause) {
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
