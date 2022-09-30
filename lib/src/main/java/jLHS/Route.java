package jLHS;

public class Route {
    public Method method;
    public String path;
    public ConnectionHandler handler;

    final public static String DEFAULT = "[\\s\\S]*";

    public Route(Method method, String path, ConnectionHandler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }
}
