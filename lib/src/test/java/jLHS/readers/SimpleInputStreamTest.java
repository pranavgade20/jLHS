package jLHS.readers;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class SimpleInputStreamTest {
    @Test
    public void testReadLine() throws IOException {
        SimpleInputStream is = new SimpleInputStream(new ByteArrayInputStream("""
                line 1\r
                line 2\r
                line 3\r
                \r
                """.getBytes()));

        Assert.assertEquals(is.readLine(), "line 1");
        Assert.assertEquals(is.readLine(), "line 2");
        Assert.assertEquals(is.readLine(), "line 3");
        Assert.assertEquals(is.readLine(), "");
        Assert.assertThrows("Reached end of stream", IOException.class, is::readLine);
    }

    @Test
    public void testReadLineThrows() throws IOException {
        SimpleInputStream is = new SimpleInputStream(new ByteArrayInputStream("""
                \r
                """.getBytes()));

        Assert.assertEquals(is.readLine(), "");
        Assert.assertThrows("Reached end of stream", IOException.class, is::readLine);
    }

    @Test
    public void testGetReadContentLength() throws IOException {
        String str = """
                \r
                """;
        SimpleInputStream is = new SimpleInputStream(new ByteArrayInputStream(str.getBytes()));

        Assert.assertEquals(is.getReadContentLength(), 0);
        is.readLine();
        Assert.assertEquals(is.getReadContentLength(), str.length());
        Assert.assertThrows("Reached end of stream", IOException.class, is::readLine);
        Assert.assertEquals(is.getReadContentLength(), str.length());
    }
}
