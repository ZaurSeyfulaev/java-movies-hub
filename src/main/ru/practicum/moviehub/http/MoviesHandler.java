package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ValidationException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class MoviesHandler extends BaseHttpHandler { // Расширьте базовый класс BaseHttpHandler
    final MoviesStore moviesStore = new MoviesStore();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        String method = ex.getRequestMethod();
        switch (method) {
            case "GET": {
                handleGet(ex, path, query);
                break;
            }
            case "POST": {
                handlePost(ex);
                break;
            }
            case "DELETE": {
                handleDelete(ex, path);
                break;
            }
        }
    }

    private void handleGet(HttpExchange ex, String path, String query) throws IOException {
        if (Pattern.matches("^/movies$", path) && query == null) {
            String json = gson.toJson(moviesStore.getAllMovies());
            send(ex, json, 200);
            return;
        }
        // /movies/{id}

        if (path.startsWith("/movies/")) {
            String pathIid = path.replaceFirst("/movies/", "");
            int id = parsePathId(pathIid);
            if (id == -1) {
                sendError(ex, 400, "Bad Request", List.of("ID должен быть целым числом"));
                return;
            }
            var movie = moviesStore.getMovieById(id);
            if (movie.isPresent()) {
                send(ex, gson.toJson(movie.get()), 200);
                return;
            } else {
                sendError(ex, 404, "Not Found", List.of("Фильм с id " + id + " не найден"));
            }
            return;
        }
        // GET /movies?year=
        if (Pattern.matches("^/movies$", path) && query.contains("year=")) {
            String year = getUrlParam(ex, "year");
            if (year == null || !year.matches("\\d+")) {
                sendError(ex, 400, "Bad Request", List.of("Некорректный параметр запроса — 'year'"));
                return;
            }
            int yearParam = Integer.parseInt(year);
            List<Movie> filtered = moviesStore.filterMovieByYear(yearParam);
            send(ex, gson.toJson(filtered), 200);
        }
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String body = read(ex);
        Movie movie;
        String contentType = ex.getRequestHeaders().getFirst("Content-Type").toString();
        if (!contentType.equals("application/json")) {
            sendError(ex, 415, "Unsupported Content-Type", List.of("Неверный Content-Type"));
            return;
        }
        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (JsonSyntaxException e) {
            sendError(ex, 400, "Bad Request", List.of("Неверный формат JSON: " + e.getMessage()));
            return;
        }
        try {
            int id = moviesStore.addNewMovie(movie);
            String jsonResponse = gson.toJson(movie);
            send(ex, jsonResponse, 201);
        } catch (ValidationException e) {
            sendError(ex, 422, "Ошибка валидации", e.getErrors());
        }
    }

    public void handleDelete(HttpExchange ex, String path) throws IOException {
        String pathIid = path.replaceFirst("/movies/", "");
        int id = parsePathId(pathIid);
        if (id == -1) {
            sendError(ex, 400, "Bad Request", List.of("ID должен быть целым числом"));
            return;
        }
        boolean deleted = moviesStore.deleteMovie(id);
        if (deleted) {
            sendNoContent(ex);
        } else {
            sendError(ex, 404, "Not Found", List.of("Фильм с id " + id + " не найден"));
        }
    }
}