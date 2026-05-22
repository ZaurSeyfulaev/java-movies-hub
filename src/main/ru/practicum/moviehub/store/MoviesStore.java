package ru.practicum.moviehub.store;

import ru.practicum.moviehub.api.ValidationException;
import ru.practicum.moviehub.model.Movie;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    int generatedId = 0;
    private Map<Integer, Movie> movies = new HashMap<>();


    private int generateId() {
        return ++generatedId;
    }

    public int addNewMovie(Movie movie) {
        int id = generateId();
        List<String> errors = new ArrayList<>();
        int year = LocalDate.now().getYear();
        if (movie.getYear() < 1888 || movie.getYear() > year + 1) {
            errors.add("год должен быть между 1888 и " + (year + 1));
        }
        if (movie.getTitle().isEmpty()) {
            errors.add("название не должно быть пустым");
        } else if (movie.getTitle().length() > 100) {
            errors.add("Слишком длинное название фильма");
        }
        if (errors.isEmpty()) {
            movie.setId(id);
            movies.put(id, movie);
            return id;
        } else {
            throw new ValidationException(errors);
        }
    }

    public boolean deleteMovie(int id) {
        if (movies.containsKey(id)) {
            movies.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public Optional<Movie> getMovieById(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> filterMovieByYear(int year) {
        return getAllMovies().stream()
                .filter(m -> m.getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies.values());
    }
}