# jLHS

jLHS(jLHS Light HTTP Server) is a fast and tiny HTTP server written in Java without any external dependencies.

## Features

- HTTP 1.1 support
- Supports SSL(https)
- Limited support for parameter&headers parsing in GET requests, to be extended to POST soon.
- Supports custom handlers to allow you to write implementations according to your needs.
- Will soon support file upload via POST requests.

## Example

```java
// create a server on the port 8080
Server server = new Server(8080);

// add a route to the server
server.on(Method.GET, // method
        "/hello", // url
        (request, response) -> { // request handler
            try {
                response.print("Hello, World!"); // sends "Hello, World!"
                response.end(); // this ends the response and closes related streams
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
```

### Documentation & Tests

TODO tbh

### Contributing

All contributions and suggestions are welcome, just remember to follow the code style. And be polite.

