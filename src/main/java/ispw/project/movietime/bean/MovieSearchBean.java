package ispw.project.movietime.bean;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class MovieSearchBean {

    private final StringProperty searchQuery;
    private final StringProperty searchError;

    private static final int MIN_SEARCH_LENGTH = 1;
    private static final int MAX_SEARCH_LENGTH = 100;

    public MovieSearchBean() {
        this.searchQuery = new SimpleStringProperty();
        this.searchError = new SimpleStringProperty();
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public StringProperty searchErrorProperty() {
        return searchError;
    }

    public String getSearchQuery() {
        return searchQuery.get();
    }

    public void setSearchQuery(String query) {
        this.searchQuery.set(query);
        validateSearchQuery();
    }

    private boolean validateSearchQuery() {
        String currentQuery = getSearchQuery();

        if (currentQuery == null || currentQuery.trim().isEmpty()) {
            searchError.set("Search query cannot be empty.");
            return false;
        }
        if (currentQuery.trim().length() < MIN_SEARCH_LENGTH) {
            searchError.set("Search query must be at least " + MIN_SEARCH_LENGTH + " character(s).");
            return false;
        }
        if (currentQuery.trim().length() > MAX_SEARCH_LENGTH) {
            searchError.set("Search query cannot exceed " + MAX_SEARCH_LENGTH + " characters.");
            return false;
        }

        searchError.set("");
        return true;
    }

    public boolean isValid() {
        return validateSearchQuery();
    }

    @Override
    public String toString() {
        return "MovieSearchBean{" +
                "searchQuery='" + searchQuery.get() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieSearchBean that = (MovieSearchBean) o;
        return Objects.equals(searchQuery.get(), that.searchQuery.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchQuery.get());
    }
}