package jLHS.http1_1server;

import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class Response implements jLHS.Response {
    protected OutputStream outputStream;
    protected PrintWriter writer = null;
    protected PrintWriter headerWriter = null;
    protected Status status = Status.WRITING_RESPONSE_CODE;
    protected final HashSet<String> defaultHeaders;
    protected ByteArrayOutputStream buf = new ByteArrayOutputStream();
    /**
     * buf might be flushed if it's size exceeds this. This is done mostly to prevent large files from
     * taking forever to be sent because they are cached in memory.
     */
    protected long default_buf_limit = 8*1024;

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
     * Sets the response code of the request. You can call this method only once per connection.
     * This method starts writing the response to the request. You can not call this method after writing headers.
     * @param statusCode the status code of the response. For example, 404
     * @param status the status of the response. For example, `Not Found`
     */
    public void setCode(int statusCode, String status) throws ProtocolFormatException {
        if (this.status != Status.WRITING_RESPONSE_CODE)
            throw new ProtocolFormatException("Response code already set", null);

        if (writer == null) writer = new PrintWriter(buf);
        if (headerWriter == null) headerWriter = new PrintWriter(outputStream);

        headerWriter.write("HTTP/1.1 " + statusCode + " " + status);
        headerWriter.write("\r\n");
        this.status = Status.WRITING_HEADERS;
        headerWriter.write("Transfer-Encoding: chunked\r\n");
        synchronized (defaultHeaders) {
            // write default headers
            for (String header : defaultHeaders) {
                headerWriter.write(header);
                headerWriter.write("\r\n");
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

        headerWriter.write(header);
        headerWriter.write("\r\n");
    }

    /**
     * Writes the given string to the response.
     * @param str
     */
    public void print(String str) throws ProtocolFormatException, IOException {
        if (status == Status.WRITING_HEADERS) {
            headerWriter.write("\r\n");
            headerWriter.flush();
            outputStream.flush();
            status = Status.WRITING_BODY;
        }

        if (status.compareTo(Status.WRITING_BODY) > 0)
            throw new ProtocolFormatException("Body already written.", null);
        if (status.compareTo(Status.WRITING_BODY) < 0) {
            setCode(200, "OK");
            headerWriter.write("\r\n\r\n");
            headerWriter.flush();
            outputStream.flush();
            status = Status.WRITING_BODY; //skipping writing headers
        }

        writer.write(str);
        if (buf.size() > default_buf_limit) flush();
    }

    /**
     * Transfers the given stream to the response.
     * @param is
     */
    public void write(InputStream is) throws ProtocolFormatException, IOException {
        if (status == Status.WRITING_HEADERS) {
            headerWriter.write("\r\n");
            headerWriter.flush();
            outputStream.flush();
            status = Status.WRITING_BODY;
        }

        if (status.compareTo(Status.WRITING_BODY) > 0)
            throw new ProtocolFormatException("Body already written.", null);
        if (status.compareTo(Status.WRITING_BODY) < 0) {
            setCode(200, "OK");
            headerWriter.write("\r\n\r\n");
            headerWriter.flush();
            outputStream.flush();
            status = Status.WRITING_BODY; //skipping writing headers
        }

        writer.flush();

        int read;
        for(byte[] buffer = new byte[8192]; (read = is.read(buffer, 0, 8192)) >= 0;) {
            buf.write(buffer, 0, read);
            if (buf.size() > default_buf_limit) flush();
        }
        if (buf.size() > default_buf_limit) flush();
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
                flush();
                outputStream.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                outputStream.flush();
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
        if (buf.size() == 0) return;
        outputStream.write((Integer.toString(buf.size(), 16) + "\r\n").getBytes(StandardCharsets.US_ASCII));
        buf.writeTo(outputStream);
        buf.reset();
        outputStream.write("\r\n".getBytes());
        outputStream.flush();
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
