package ispw.project.movietime.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String username;

    private List<MovieModel> movies;

    public ListModel() {
        this.movies = new ArrayList<>();
    }

    public ListModel(int id, String name, String username) {
        this();
        this.id = id;
        this.name = name;
        this.username = username;
    }

    public ListModel(int id, String name, String username, java.util.List<MovieModel> movies) {
        this(id, name, username);
        this.movies = (movies != null) ? new ArrayList<>(movies) : new ArrayList<>();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public boolean addMovie(MovieModel movie) {
        if (movie == null || this.movies.contains(movie)) {
            return false;
        }
        return this.movies.add(movie);
    }

    public boolean removeMovie(MovieModel movie) {
        if (movie == null) {
            return false;
        }
        return this.movies.remove(movie);
    }

    public boolean containsMovie(MovieModel movie) {
        if (movie == null) {
            return false;
        }
        return this.movies.contains(movie);
    }

    public java.util.List<MovieModel> getMovies() {
        return Collections.unmodifiableList(this.movies);
    }

    public void setMovies(java.util.List<MovieModel> movies) {
        this.movies = (movies != null) ? new ArrayList<>(movies) : new ArrayList<>();
    }

    public void rename(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("List name cannot be empty.");
        }
        this.name = newName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListModel listModel = (ListModel) o;
        return id == listModel.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ListModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", movieCount=" + movies.size() +
                '}';
    }
}