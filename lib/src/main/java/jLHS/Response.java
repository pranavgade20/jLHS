package jLHS;

import jLHS.exceptions.ProtocolFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Response {
    OutputStream getOutputStream() throws ProtocolFormatException;
    void setCode(int statusCode, String status) throws ProtocolFormatException;
        default void writeHeader(String headerName, String value) throws ProtocolFormatException {
        writeHeader(headerName + ": " + value);
    }
    void writeHeader(String header) throws ProtocolFormatException;
    void print(String str) throws ProtocolFormatException, IOException;
    void write(InputStream is) throws ProtocolFormatException, IOException;
    void end() throws IOException, ProtocolFormatException;
    void flush() throws IOException;

    Status getStatus();
    enum Status {
        WRITING_RESPONSE_CODE,
        WRITING_HEADERS,
        WRITING_BODY,
        ENDED_RESPONSE
    }
}
