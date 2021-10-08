package jLHS;

import java.io.IOException;

public interface Server {
    void start();

    void stop() throws IOException;

    void on(Method method, String pathRegex, ConnectionHandler handler);
}
