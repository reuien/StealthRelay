package exceptions;

/**
 * Exception that gets raised if something went wrong in the server communication and an asset
 * could not be stored or in general if a operation could not be persisted.
 */
public class CouldNotStoreException extends WriteException {

    public CouldNotStoreException(String reason) {
        super(reason);
    }

}
