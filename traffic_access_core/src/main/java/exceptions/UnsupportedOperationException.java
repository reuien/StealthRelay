package exceptions;

/**
 * Exception to be thrown if a TimeCrypt server does not support certain operations.
 */
public class UnsupportedOperationException extends Exception {

    // TODO: This is a workaround for shortcomings of clients and should be removed if possible.
    public UnsupportedOperationException(String message) {
        super(message);
    }
}
