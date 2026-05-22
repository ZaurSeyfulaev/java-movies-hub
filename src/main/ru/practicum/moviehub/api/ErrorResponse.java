package ru.practicum.moviehub.api;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    String error;
    private List<String> details = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(String error, List<String> details) {
        this.error = error;
        this.details = details;
    }

    public void clearError() {
        this.error = null;
        this.details.clear();
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(String error) {
        details.add(error);
    }

    public String getErrors() {
        return error;
    }
}