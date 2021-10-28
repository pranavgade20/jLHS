package jLHS.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipInputStream extends SimpleInputStream {
    public GzipInputStream(InputStream in) throws IOException {
        super(new GZIPInputStreamWrapper(in));
    }

    @Override
    public int getReadContentLength() {
        return ((GZIPInputStreamWrapper)super.in).getContentLength();
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
