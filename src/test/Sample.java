package test;

import jLHS.Method;
import jLHS.POSTRequestHandler;
import jLHS.Route;
import jLHS.Server;

import java.io.*;
import java.util.Map;

public class Sample {
    static Server server;

    public static void main(String[] args) throws Exception {
        server = new Server(8081);

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

        server.on(Method.POST,
                "/post",
                (request, response) -> {
                    try {
                        POSTRequestHandler h = new POSTRequestHandler(request);
                        InputStreamReader isr = new InputStreamReader(h.getFormData("data").resourceStream);
//                        isr.transferTo(new OutputStreamWriter(System.out));
                        BufferedReader reader = new BufferedReader(isr);
                        String l = reader.readLine();
                        System.out.println(l);
//                        request.getRequestReader().transferTo(new OutputStreamWriter(System.out));
                        String line;
                        response.print("hi");

//                        System.out.println("=========================");
//                        while ((line = request.getRequestReader().readLine()) != null && !line.isEmpty()) {
//                            System.out.println(line);
//                        }
//                        System.out.println("=========================");
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        server.on(Method.GET,
                Route.DEFAULT,
                ((request, response) -> {
                    try {
                        File file = new File(request.path);
                        if (!file.exists()) {
                            response.setCode(404, "Not Found");
                            response.writeHeader("content-type", "text/html");
                            response.print("The requested file was not found.");
                        } else {
                            if (file.isDirectory()) {
                                response.writeHeader("content-type", "text/html");
                                for (File f : file.listFiles()) {
                                    if (f.isFile()) {
                                        response.print("<a href=\"" + f.getPath() + "\">" + f.getName() + "</a>");
                                        response.print("<br>");
                                    } else if (f.isDirectory()) {
                                        response.print("<a href=\"" + f.getPath() + "\">" + f.getName() + '/' + "</a>");
                                        response.print("<br>");
                                    }
                                }
                                response.print("<hr>");
                                response.print(
                                        "<div class=\"form\">\n" +
                                                "<form action=\"fileupload\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
                                                "<input type=\"file\" name=\"filetoupload\" required>\n" +
                                                "<label>Choose file...</label><br>\n" +
                                                "<input type=\"submit\" name=\"button\" text=\"Upload!\">\n" +
                                                "</form>\n" +
                                                "</div>");

                            }
                        }
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

        server.on(Method.POST,
                Route.DEFAULT,
                ((request, response) -> {
                    try {
//                        POSTRequestHandler handler = new POSTRequestHandler(request);
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(handler.getFormData("filetoupload").resourceStream));
//                        String line;
//                        while ((line = reader.readLine()) != null) response.print(line);
//                        response.end();
//                        request.getStream().transferTo(System.out);
                        response.print("OK");
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
        server.start();
        System.out.println("started");
    }
}