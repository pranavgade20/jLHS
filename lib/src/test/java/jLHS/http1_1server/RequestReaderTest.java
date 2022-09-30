package jLHS.http1_1server;

import jLHS.Method;
import jLHS.RequestReader;
import jLHS.exceptions.MalformedRequestException;
import jLHS.exceptions.ProtocolFormatException;
import jLHS.readers.SimpleInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RequestReaderTest {
    @Test
    public void getRequestTest() throws MalformedRequestException, ProtocolFormatException, IOException {
        String req = """
                GET /get HTTP/1.1\r
                Header: value\r
                \r
                """;
        SimpleInputStream is = new SimpleInputStream(new ByteArrayInputStream(req.getBytes()));

        RequestReader reader = new jLHS.http1_1server.RequestReader(is);

        Assert.assertEquals(reader.getPath(), "/get");
        Assert.assertEquals(reader.getMethod(), Method.GET);
        Assert.assertEquals(reader.getHeader("Header"), "value");
    }
}
