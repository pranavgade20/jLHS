package jLHS.http1_1server;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class RequestInputStream extends BufferedInputStream {

    public RequestInputStream(InputStream in) {
        super(in);
    }

    public int getPos() {
        return pos;
    }
    public void setPos(int pos) {
        this.pos = pos;
    }
    public int getCount() {
        return count;
    }
    public byte[] getBuf() {
        return buf;
    }
}
