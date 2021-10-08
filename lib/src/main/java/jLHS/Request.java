package jLHS;

import java.util.HashMap;

public interface Request {
    Method getMethod();
    String getPath();
    HashMap<String, String> getParams();
    default String getParam(String param) {
        return getParams().get(param);
    }
    HashMap<String, String> getHeaders();
    default String getHeader(String header) {
        return getHeaders().get(header);
    }
    RequestReader getRequestReader();
}
