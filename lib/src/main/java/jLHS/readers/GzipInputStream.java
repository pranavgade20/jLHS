package jLHS.readers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipInputStream extends SimpleInputStream {
    boolean filled = false;
    public GzipInputStream(InputStream in) throws IOException {
        super(new GZIPInputStreamWrapper(in));
    }

    @Override
    public int getReadContentLength() {
        return ((GZIPInputStreamWrapper)super.in).getContentLength();
    }

    @Override
    public long fillCompletely() throws IOException {
        if (filled) return 0;
        super.in = new ByteArrayInputStream(super.in.readAllBytes());
        filled = true;
        return getReadContentLength();
    }

    private static class GZIPInputStreamWrapper extends GZIPInputStream {
        public GZIPInputStreamWrapper(InputStream in, int size) throws IOException {
            super(in, size);
        }

        public GZIPInputStreamWrapper(InputStream in) throws IOException {
            super(in);
        }

        public int getContentLength() {
            return this.len;
        }

    }
}
