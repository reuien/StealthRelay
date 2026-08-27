package exceptions;

public class StorageException extends RequestException {
    public StorageException(String message, int id) {
        super(message, id);
    }
}
