package ee.taltech.fooddeliveryapp.exceptions;

/**
 * Thrown when a request is made, but the weather conditions forbid delivery with selected vehicle.
 */
public class VehicleForbiddenException extends Throwable {
    private final String message;
    private final Throwable cause;

    public VehicleForbiddenException(String message) {
        this(message, null);
    }

    public VehicleForbiddenException(String message, Throwable cause) {
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
