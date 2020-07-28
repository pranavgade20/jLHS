package test;

import jLHS.Method;
import jLHS.Route;
import jLHS.Server;

import java.util.Map;

public class Sample {
    static Server server;

    public static void main(String[] args) throws Exception {
        server = new Server(8080);
        server.on(Method.GET,
                "/hello",
                (request, response) -> {
                    try {
                        response.print("Hello, World!");
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        server.on(Method.GET,
                "/params",
                (request, response) -> {
                    try {
                        response.writeHeader("content-type", "text/html");
                        for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
                            response.print(entry.getKey() + ":" + entry.getValue() + "<br>");
                        }
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        server.on(Method.GET,
                Route.DEFAULT,
                ((request, response) -> {
                    try {
                        response.setCode(404, "Not Found");
                        response.writeHeader("content-type", "text/html");
                        response.print("The requested URL was not found on this server.");
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
        server.start();
        System.out.println("started");
    }
}