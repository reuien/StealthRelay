package exceptions;

/**
 * Error that indicates that the provided data point can not be written to the stream because
 * it is not in the current write window.
 */
public class DataPointOutsideOfWriteWindowException extends WriteException {
    public DataPointOutsideOfWriteWindowException(String message) {
        super(message);
    }
}
