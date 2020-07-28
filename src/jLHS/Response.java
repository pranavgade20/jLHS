package jLHS;

import jLHS.exceptions.ProtocolFormatException;

import java.io.*;
import java.net.Socket;

public class Response {
    private OutputStream outputStream;
    private PrintWriter writer = null;
    private boolean headersWritten = false;

    public Response(Socket clientSocket) throws IOException {
        outputStream = clientSocket.getOutputStream();
    }

    /**
     * Get the associated OutputStream. It is recommended that you use a handler to perform operations on this stream.
     * @return the associated OutputStream.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the response code of the request. You can call this method only once per connection.
     * This method starts writing the response to the request. You can not call this method after writing headers.
     * @param statusCode the status code of the response. For example, 404
     * @param status the status of the response. For example, `Not Found`
     */
    public void setCode(int statusCode, String status) {
        if (writer == null) {
            writer = new PrintWriter(new DataOutputStream(outputStream));
        }

        writer.write("HTTP/1.1 " + statusCode + " " + status);
        writer.write("\r\n");
    }

    public void writeHeader(String headerName, String value) throws ProtocolFormatException {
        writeHeader(headerName + ": " + value);
    }

    public void writeHeader(String header) throws ProtocolFormatException {
        if (writer == null) {
            setCode(200, "OK");
        }
        if (headersWritten) throw new ProtocolFormatException("Cannot write headers after writing the response.", null);

        writer.write(header);
        writer.write("\r\n");
    }

    /**
     * Writes the given string to the response.
     * @param str
     */
    public void print(String str) {
        if (writer == null) {
            setCode(200, "OK");
        }
        if (!headersWritten) {
            headersWritten = true;
            writer.write("\r\n");
        }
        writer.write(str);
    }

    /**
     * Ends and sends the response, closing and flushing the associated streams.
     * @throws IOException
     */
    public void end() throws IOException{
        writer.write("\r\n\r\n");
        flush();
        outputStream.close();
    }

    /**
     * Flushes the underlying streams.
     * @throws IOException
     */
    public void flush() throws IOException{
        writer.flush();
        outputStream.flush();
    }
}
