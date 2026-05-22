package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8"; // !!! Укажите содержимое заголовка Content-Type
    protected Gson gson = new Gson();

    protected int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected String getUrlParam(HttpExchange ex, String param) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) {
            return null;
        }
        String[] pairsParam = query.split("&");
        for (String pair : pairsParam) {
            int index = pair.indexOf("=");
            if (index != -1) {
                String key = pair.substring(0, index);
                if (key.equals(param)) {
                    String value = pair.substring(index + 1);
                    return URLDecoder.decode(value, UTF_8);
                } else if (param.equals(key)) {
                    return "";
                }
            }
        }
        return null;
    }

    protected String read(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void send(HttpExchange ex, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
        ex.close();
    }

    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendError(HttpExchange ex, int statusCode, String errorMessage, List<String> details) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(errorMessage);
        for (String detail : details) {
            errorResponse.setDetails(detail);
        }
        send(ex, gson.toJson(errorResponse), statusCode);
    }
}