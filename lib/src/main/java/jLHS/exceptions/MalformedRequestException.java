package jLHS.exceptions;

public class MalformedRequestException extends Exception {
    public MalformedRequestException(String message, Throwable err) {
        super(message, err);
    }
}
