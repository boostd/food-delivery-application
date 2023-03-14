package ee.taltech.fooddeliveryapp.service;

public class UnknownVehicleException extends Throwable {
    private final String message;


    private final Throwable cause;

    UnknownVehicleException(String message) {
        this(message, null);
    }

    UnknownVehicleException(String message, Throwable cause) {
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
