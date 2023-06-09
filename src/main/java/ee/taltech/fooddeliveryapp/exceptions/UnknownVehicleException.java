package ee.taltech.fooddeliveryapp.exceptions;

/**
 * Thrown when a request is made for an unknown vehicle.
 */
public class UnknownVehicleException extends Throwable {
    private final String message;
    private final Throwable cause;

    public UnknownVehicleException(String message) {
        this(message, null);
    }

    public UnknownVehicleException(String message, Throwable cause) {
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
