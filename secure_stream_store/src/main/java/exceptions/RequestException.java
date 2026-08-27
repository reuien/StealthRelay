package exceptions;

import dataServerProtocol.DataServerProtocol.ErrorResponse;

public class RequestException extends RuntimeException {

    int id = 0;

    public RequestException(String message, int id) {
        super(message);
        this.id = id;
    }

    public ErrorResponse getErrorRespons() {
        return ErrorResponse.newBuilder().setId(id).setMessage(this.getMessage()).build();
    }

}
