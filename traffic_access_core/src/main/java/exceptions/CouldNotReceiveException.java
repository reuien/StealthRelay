package exceptions;

/**
 * Exception for problems in the interaction with a TimeCrypt server. Throwing an exception like this should indicate
 * that fetching the requested resource failed.
 */
public class CouldNotReceiveException extends Exception {
    public CouldNotReceiveException(String reason) {
        super(reason);
    }
}
