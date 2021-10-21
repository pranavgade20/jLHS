package jLHS.http1_1server;

import jLHS.ConnectionHandler;
import jLHS.Method;
import jLHS.Route;
import jLHS.exceptions.ProtocolFormatException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import java.util.regex.Pattern;

public class Server implements jLHS.Server {
    protected final ServerSocket serverSocket;
    protected final LinkedList<Thread> serverThreads = new LinkedList<>();
    protected ArrayList<Route> routes;
    protected HashSet<String> defaultHeaders = new HashSet<>();
    public int MAX_CONCURRENT_CONNECTIONS = 8;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        routes = new ArrayList<>();
    }

    public Server(int port, String keystorePath, String password) throws Exception{
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;

        ctx = SSLContext.getInstance("TLS");
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("JKS");

        ks.load(new FileInputStream(keystorePath), password.toCharArray());
        kmf.init(ks, password.toCharArray());
        ctx.init(kmf.getKeyManagers(), null, null);

        serverSocket = ctx.getServerSocketFactory().createServerSocket(port);

        ((SSLServerSocket)serverSocket).setNeedClientAuth(false);
    }

    public void addDefaultHeaders(Collection<String> defaultHeaders) {
        this.defaultHeaders.addAll(defaultHeaders);
    }

    public HashSet<String> getDefaultHeaders() {
        return defaultHeaders;
    }

    @Override
    public void start() {
        start(MAX_CONCURRENT_CONNECTIONS);
    }

    public void start(int concurrent_connections) {
        for (int i = 0; i < concurrent_connections; i++) {
            Thread thread = new ServerThread();
            synchronized (serverThreads) {
                serverThreads.add(thread);
            }
            thread.start();
        }
    }

    @Deprecated
    @Override
    public void stop() throws IOException {
        synchronized (serverThreads) {
            serverThreads.forEach(t -> {
                t.interrupt();
                t.stop();
            });
            serverThreads.clear();
        }
        serverSocket.close();
    }

    @Override
    public void on(Method method, String path, ConnectionHandler handler) {
        routes.add(new Route(method, path.split("\\?")[0], handler));
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Socket client;
                    synchronized (serverSocket) {
                        client = serverSocket.accept();
                    }
                    RequestBuilder builder = new RequestBuilder(client);
                    try {
                        for (Request request : builder) {
                            Response response = new Response(client);
                            boolean handled = false;

                            for (Route route : routes) {
                                if (route.method == request.getMethod()) {
                                    String requestPath = request.getPath().split("\\?")[0];
                                    if (Pattern.matches(route.path, requestPath)) {
                                        if ("100-continue".equals(request.getHeaders().get("Expect"))) {
                                            client.getOutputStream().write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                                            client.getOutputStream().flush();
                                        }
                                        route.handler.handler(request, response);
                                        if (response.getStatus() != jLHS.Response.Status.ENDED_RESPONSE)
                                            response.end();
                                        handled = true;
                                        break;
                                    }
                                }
                            }

                            if (!handled) {
                                response.setCode(404, "Not Found");
                                response.writeHeader("content-type", "text/html");
                                response.print("Error 404 : The requested url was not found on the server.");
                                response.end();
                            }
                            response.flush();
                        }
                    } catch (SocketException ignored) {
                    } catch (NoSuchElementException | IOException | ProtocolFormatException e) {
                        new PrintWriter(client.getOutputStream()).print(
                                "HTTP/1.1 500 Internal Server Error\r\n" +
                                        "Connection: close\r\n\r\n"
                        );
                        client.close();
                    }

                } catch (ThreadDeath e) {
                    // stopped the thread
                } catch (Exception exception) {
                    exception.printStackTrace();
                    if (serverSocket.isClosed()) return;
                    Thread thread = new ServerThread();
                    synchronized (serverThreads) {
                        serverThreads.remove(this);
                        serverThreads.add(thread);
                    }
                    thread.start();
                }
            }
        }
    }
}
