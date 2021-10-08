package jLHS.simpleserver;

import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;

public class Response implements jLHS.Response {
    protected OutputStream outputStream;
    protected PrintWriter writer = null;
    protected Status status = Status.WRITING_RESPONSE_CODE;
    protected final HashSet<String> defaultHeaders;

    public Response(Socket clientSocket) throws IOException {
        this(clientSocket, new HashSet<>());
    }

    public Response(Socket clientSocket, HashSet<String> defaultHeaders) throws IOException {
        outputStream = clientSocket.getOutputStream();
        this.defaultHeaders = defaultHeaders;
    }

    public void addDefaultHeader(String headerName, String value) {
        addDefaultHeader(headerName + ": " + value);
    }

    public void addDefaultHeader(String header) {
        synchronized (defaultHeaders) {
            defaultHeaders.add(header);
        }
    }

    public HashSet<String> getDefaultHeaders() {
        return defaultHeaders;
    }
    /**
     * Get the associated OutputStream. It is recommended that you use a handler to perform operations on this stream.
     * @return the associated OutputStream.
     */
    public OutputStream getOutputStream() throws ProtocolFormatException {
        if (status == Status.WRITING_HEADERS) {
            writer.write("\r\n");
            status = Status.WRITING_BODY;
        }

        if (status.compareTo(Status.WRITING_BODY) > 0)
            throw new ProtocolFormatException("Body already written.", null);
        if (status.compareTo(Status.WRITING_BODY) < 0) {
            setCode(200, "OK");
            writer.write("\r\n\r\n");
            status = Status.WRITING_BODY; //skipping writing headers
        }

        return outputStream;
    }

    /**
     * Sets the response code of the request. You can call this method only once per connection.
     * This method starts writing the response to the request. You can not call this method after writing headers.
     * @param statusCode the status code of the response. For example, 404
     * @param status the status of the response. For example, `Not Found`
     */
    public void setCode(int statusCode, String status) throws ProtocolFormatException {
        if (this.status != Status.WRITING_RESPONSE_CODE)
            throw new ProtocolFormatException("Response code already set", null);

        if (writer == null) {
            writer = new PrintWriter(outputStream);
        }

        writer.write("HTTP/1.0 " + statusCode + " " + status);
        writer.write("\r\n");
        this.status = Status.WRITING_HEADERS;
        synchronized (defaultHeaders) {
            // write default headers
            for (String header : defaultHeaders) {
                writer.write(header);
                writer.write("\r\n");
            }
        }
    }

    public void writeHeader(String headerName, String value) throws ProtocolFormatException {
        writeHeader(headerName + ": " + value);
    }

    public void writeHeader(String header) throws ProtocolFormatException {
        if (status.compareTo(Status.WRITING_HEADERS) > 0)
            throw new ProtocolFormatException("Headers already written.", null);
        if (status.compareTo(Status.WRITING_HEADERS) < 0)
            setCode(200, "OK");

        writer.write(header);
        writer.write("\r\n");
    }

    /**
     * Writes the given string to the response.
     * @param str
     */
    public void print(String str) throws ProtocolFormatException, IOException {
        if (status == Status.WRITING_HEADERS) {
            writer.write("\r\n");
            status = Status.WRITING_BODY;
        }

        if (status.compareTo(Status.WRITING_BODY) > 0)
            throw new ProtocolFormatException("Body already written.", null);
        if (status.compareTo(Status.WRITING_BODY) < 0) {
            setCode(200, "OK");
            writer.write("\r\n\r\n");
            status = Status.WRITING_BODY; //skipping writing headers
        }

        writer.write(str);
        writer.flush();
        outputStream.flush();
    }

    /**
     * Transfers the given stream to the response.
     * @param is
     */
    public void write(InputStream is) throws ProtocolFormatException, IOException {
        if (status == Status.WRITING_HEADERS) {
            writer.write("\r\n");
            status = Status.WRITING_BODY;
        }

        if (status.compareTo(Status.WRITING_BODY) > 0)
            throw new ProtocolFormatException("Body already written.", null);
        if (status.compareTo(Status.WRITING_BODY) < 0) {
            setCode(200, "OK");
            writer.write("\r\n\r\n");
            status = Status.WRITING_BODY; //skipping writing headers
        }

        writer.flush();
        is.transferTo(outputStream);
        outputStream.flush();
    }

    /**
     * Ends and sends the response, closing and flushing the associated streams.
     * @throws IOException
     */
    public void end() throws IOException, ProtocolFormatException {
        switch (status) {
            case WRITING_RESPONSE_CODE:
                setCode(500, "Internal Server Error");
                print("Internal Server Error");
                end();
                break;
            case WRITING_HEADERS:
                print("");
                end();
                break;
            case WRITING_BODY:
                writer.write("\r\n\r\n");
                flush();
                outputStream.close();
                status = Status.ENDED_RESPONSE;
                break;
            case ENDED_RESPONSE:
                throw new ProtocolFormatException("Response already ended.", null);
        }
    }

    /**
     * Flushes the underlying streams.
     * @throws IOException
     */
    public void flush() throws IOException {
        if (status == Status.ENDED_RESPONSE) return;
        writer.flush();
        outputStream.flush();
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
