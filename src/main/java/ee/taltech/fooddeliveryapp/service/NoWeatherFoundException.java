package ee.taltech.fooddeliveryapp.service;

public class NoWeatherFoundException extends Throwable {
    private final String message;
    private final Throwable cause;

    NoWeatherFoundException(String message) {
        this(message, null);
    }

    NoWeatherFoundException(String message, Throwable cause) {
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
