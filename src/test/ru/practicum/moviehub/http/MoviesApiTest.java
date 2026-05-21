package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";// !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson;

    @BeforeEach
     void beforeAll() {
        // !!! Реализуйте метод beforeAll
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        gson = new Gson();
    }

    @AfterEach
     void afterAll() {
        server.stop();
    }

    @Test
    void getMoviesWhenEmptyReturnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMoviesShouldReturnMovies() throws Exception {
        addMovie();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode());
        Movie[] movies = gson.fromJson(resp.body(), Movie[].class);
        assertEquals(2, movies.length);
    }

    @Test
    void shouldBeAddedMovies() throws Exception {
        Movie movie = new Movie(1997, "Титаник");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(201, response.statusCode());
        assertEquals("1997", responseBody.get("year").getAsString());
        assertEquals("Титаник", responseBody.get("title").getAsString());
    }

    @Test
    void shouldReturnErrorIfTitleIsEmpty() throws Exception {
        Movie movie = new Movie(1997, "");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(422, response.statusCode());
        assertEquals("Ошибка валидации", responseBody.get("error").getAsString());
        assertEquals("название не должно быть пустым", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnErrorIfTitleMoreThan100Characters() throws Exception {
        Movie movie = new Movie(1997, "12345678901234567890123456789012345678901234567890" +
                "123456789012345678901234567890123456789012345678901");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(422, response.statusCode());
        assertEquals("Ошибка валидации", responseBody.get("error").getAsString());
        assertEquals("Слишком длинное название фильма", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnErrorIfMovieWhenYearIsBefore1888() throws Exception {
        Movie movie = new Movie(1887, "Titanic");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(422, response.statusCode());
        assertEquals("Ошибка валидации", responseBody.get("error").getAsString());
        assertEquals("год должен быть между 1888 и 2026", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnErrorIfMovieWhenYearIsAfterCurrentYearPlusOne() throws Exception {
        int currentYear = LocalDate.now().getYear();
        Movie movie = new Movie(currentYear + 2, "Titanic");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(422, response.statusCode());
        assertEquals("Ошибка валидации", responseBody.get("error").getAsString());
        assertEquals("год должен быть между 1888 и 2026", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnErrorIfContentTypeIsNotAppliecationJson() throws Exception {
        int currentYear = LocalDate.now().getYear();
        Movie movie = new Movie(currentYear, "Titanic");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/html")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
        assertEquals(415, response.statusCode());
        assertEquals("Unsupported Content-Type", responseBody.get("error").getAsString());
        assertEquals("Неверный Content-Type", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnMovieById() throws Exception {
        addMovie();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 1)) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals(200, resp.statusCode());
        assertEquals("Титаник", responseBody.get("title").getAsString());
    }

    @Test
    void shouldReturnErrorIfMovieNotFoundById() throws Exception {
        addMovie();
        int filmId = 100;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + filmId)) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals(404, resp.statusCode());
        assertEquals("Фильм с id " + filmId + " не найден", responseBody.get("details").getAsString());
    }


    @Test
    void shouldReturnErrorIfMovieIdIsNotInteger() throws Exception {
        addMovie();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + "ABC"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals(400, resp.statusCode());
        assertEquals("ID должен быть целым числом", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnMovieByYear() throws Exception {
        addMovie();
        int year = 1997;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = resp.body().trim();
        Movie[] movies = gson.fromJson(body, Movie[].class);
        assertEquals(200, resp.statusCode());
        assertEquals(1, movies.length);
        assertEquals("Титаник", movies[0].getTitle());
    }

    @Test
    void shouldReturnErrorWhenNoMoviesMatchGivenYear() throws IOException, InterruptedException {
        addMovie();
        int year = 1999;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        JsonObject responseBody = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals(404, resp.statusCode());
        assertEquals("В коллекции нет фильмов с указанным годом", responseBody.get("details").getAsString());
    }

    @Test
    void shouldReturnErrorIfMovieYearIncorrectM() throws IOException, InterruptedException {
        addMovie();
        String  year = "f";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject responseBody = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals(400, resp.statusCode());
        assertEquals("Некорректный параметр запроса — 'year'", responseBody.get("details").getAsString());
    }

    @Test
   void shouldDeleteMovieWhenIdExists() throws IOException, InterruptedException {
        addMovie();
       int id = 1;
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = deleteResponse.statusCode();
        JsonObject responseBody = gson.fromJson(deleteResponse.body(), JsonObject.class);
        assertEquals(204, statusCode);

    }

    @Test
    void shouldDeleteMovieIfIdNotFound() throws IOException, InterruptedException {
        addMovie();
        int id = 111;
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = deleteResponse.statusCode();
        JsonObject responseBody = gson.fromJson(deleteResponse.body(), JsonObject.class);
        assertEquals(404, statusCode);
        assertEquals("Фильм с id "+ 111 + " не найден",  responseBody.get("details").getAsString());
    }

    @Test
    void shouldDeleteMovieIfIdIncorrect() throws IOException, InterruptedException {
        addMovie();
        String id = "aa";
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = deleteResponse.statusCode();
        JsonObject responseBody = gson.fromJson(deleteResponse.body(), JsonObject.class);
        assertEquals(400, statusCode);
        assertEquals("ID должен быть целым числом",  responseBody.get("details").getAsString());

    }
    private void addMovie() throws IOException, InterruptedException {
        Movie movie1 = new Movie(1997, "Титаник");
        Movie movie2 = new Movie(2001, "Шрек");

        HttpRequest postMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .build();
        HttpResponse<String> resp1 = client.send(postMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest postMovie2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie2)))
                .build();
        HttpResponse<String> resp2 = client.send(postMovie2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}