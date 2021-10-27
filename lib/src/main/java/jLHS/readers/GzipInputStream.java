package jLHS.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipInputStream extends SimpleInputStream {
    public GzipInputStream(InputStream in) throws IOException {
        super(new GZIPInputStream(in));
    }
}
