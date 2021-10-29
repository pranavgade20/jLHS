package jLHS.http1_1server;

import jLHS.Method;
import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.exceptions.URLFormatException;
import jLHS.readers.FixedLengthInputStream;
import jLHS.readers.GzipInputStream;
import jLHS.readers.SimpleInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class RequestReader implements jLHS.RequestReader {
    SimpleInputStream inputStream;
    HashMap<String, String> headers = new HashMap<>();
    String path;
    HashMap<String, String> params = new HashMap<>();
    Method method;
    boolean gzip = false;

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public Method getMethod() {
        return method;
    }

    public RequestReader(SimpleInputStream inputStream) throws MalformedRequestException, ProtocolFormatException, IOException {
        this.inputStream = inputStream;
        try {
            String line = inputStream.readLine();
            path = line.split(" ")[1];
            method = Method.valueOf(line.split(" ")[0]);

            parseParams(path);
        } catch (Exception exception) {
            throw new MalformedRequestException("The client request does not follow the HTTP protocol.", exception);
        }

        parseHeaders();

        if (method == Method.POST) {
            parseFormData();
        }
    }

    /**
     * Parses the headers of the http request.
     * @throws MalformedRequestException
     */
    protected void parseHeaders() throws MalformedRequestException{
        try {
            String line;
            while ((line = inputStream.readLine()) != null && !line.isEmpty()) {
                String header = line.split(": ")[0];
                String value = line.split(": ")[1];
                headers.put(header, value);
            }
        } catch (Exception exception) {
            throw new MalformedRequestException("The client request does not follow HTTP protocol.", exception);
        }
    }

    /**
     * Parsed the parameters from the request path.
     * @param requestPath
     * @throws URLFormatException
     */
    //TODO: support params starting with & etc
    protected void parseParams(String requestPath) throws URLFormatException{
        try {
            String[] list = requestPath.substring(requestPath.lastIndexOf("/")+1).split("\\?");
            for (int i = 1; i < list.length; i++) {
                if (list[i] != null && !list[i].isEmpty()) {
                    String key = list[i].split("=")[0];
                    String value = list[i].split("=")[1];
                    params.put(key, value);
                }
            }
        } catch (StringIndexOutOfBoundsException indexOutOfBoundsException) {
            //we are cool, do nothing
        } catch (Exception exception) {
            throw new URLFormatException("Error parsing URL", exception);
        }
    }

    protected long content_length;
    long read_content_count = 0;
    protected String boundary = null;
    protected HashMap<String, FormData> cache = new HashMap<>();
    public void parseFormData() throws ProtocolFormatException, IOException {
        if ("gzip".equals(headers.get("Content-Encoding"))) gzip = true;
        try {
            if (!gzip) boundary = "--" + headers.get("Content-Type").split("=")[1];
        } catch (Exception e) {
            throw new ProtocolFormatException("Malformed headers: Expected header describing the boundary of data being sent.", e);
        }
        try {
            content_length = Long.parseLong(headers.get("Content-Length"));
        } catch (Exception e) {
            throw new ProtocolFormatException("Malformed headers: Expected header describing the length of data being sent. Chunked encoding is not supported yet.", e);
        }
        read_content_count = 0;

        if (!gzip) while (!inputStream.readLine().equals(boundary)) ;
    }

    public Optional<FormData> getFormData(String name) throws IOException, ProtocolFormatException {
        if (cache.containsKey(name)) return Optional.of(cache.get(name));
        return findFormData(name);
    }

    public void fillCompletely() throws IOException, ProtocolFormatException {
        findFormData(null);
    }

    protected Optional<FormData> findFormData(String name) throws IOException, ProtocolFormatException {
        for (FormData formData : cache.values()) {
            read_content_count += formData.getFormData().fillCompletely();
        }
        while (read_content_count < content_length) {
            if (gzip) {
                return Optional.of(new FormData(new HashMap<>(), new GzipInputStream(inputStream)));
            }
            HashMap<String, String> headers = new HashMap<>();
            String line;
            while (!(line = inputStream.readLine()).isEmpty()) {
                read_content_count += inputStream.getReadContentLength();
                String[] h = line.split(": ");
                headers.put(h[0], h[1]);
            }
            FormData formData = new FormData(headers, new FixedLengthInputStream((boundary).getBytes(StandardCharsets.US_ASCII), inputStream));

            if (!headers.containsKey("Content-Disposition"))
                throw new ProtocolFormatException("Malformed headers: Expected header describing the type of content.", null);
            String[] content_disposition = headers.get("Content-Disposition").split("; ");
            String content_name = Arrays.stream(content_disposition).filter(s -> s.startsWith("name=\"")).findAny().orElseThrow();
            content_name = content_name.substring("name=\"".length(), content_name.length() - 1);
            cache.put(content_name, formData);
            if (content_name.equals(name)) {
                return Optional.of(formData);
            } else {
                read_content_count += formData.getFormData().fillCompletely();
            }
        }
        return Optional.empty();
    }

    public class FormData {
        HashMap<String, String> headers;
        SimpleInputStream formData;

        public FormData(HashMap<String, String> headers, SimpleInputStream formData) {
            this.formData = formData;
            this.headers = headers;
        }

        public HashMap<String, String> getHeaders() {
            return headers;
        }

        public SimpleInputStream getFormData() {
            return formData;
        }
    }
}
