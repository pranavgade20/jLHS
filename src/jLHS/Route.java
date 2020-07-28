package jLHS;

public class Route {
    Method method;
    String path;
    ConnectionHandler handler;

    final public static String DEFAULT = "[\\s\\S]*";

    Route(Method method, String path, ConnectionHandler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }
}
