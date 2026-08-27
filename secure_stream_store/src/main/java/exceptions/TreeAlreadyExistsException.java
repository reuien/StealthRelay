package exceptions;

public class TreeAlreadyExistsException extends TreeException {
    public TreeAlreadyExistsException(String message, int id) {
        super(message, id);
    }
}
