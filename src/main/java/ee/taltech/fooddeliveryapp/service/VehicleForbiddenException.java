package ee.taltech.fooddeliveryapp.service;

public class VehicleForbiddenException extends Throwable {
    private final String message;
    private final Throwable cause;

    VehicleForbiddenException(String message) {
        this(message, null);
    }

    VehicleForbiddenException(String message, Throwable cause) {
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
