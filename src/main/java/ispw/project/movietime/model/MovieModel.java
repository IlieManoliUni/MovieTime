package ispw.project.movietime.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MovieModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String title;

    @SerializedName("original_title")
    private String originalTitle;

    @SerializedName("original_language")
    private String originalLanguage;

    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("vote_count")
    private int voteCount;

    private int runtime;

    private double popularity;

    private String status;

    private String tagline;

    @SerializedName("imdb_id")
    private String imdbId;

    private long budget;
    private long revenue;

    private List<Genre> genres;
    private List<ProductionCompany> productionCompanies;
    private List<ProductionCountry> productionCountries;
    private List<SpokenLanguage> spokenLanguages;

    private static final String NAME_FIELD_PREFIX = ", name='";

    public MovieModel() {
        this.genres = new ArrayList<>();
        this.productionCompanies = new ArrayList<>();
        this.productionCountries = new ArrayList<>();
        this.spokenLanguages = new ArrayList<>();
    }

    public MovieModel(int id, int runtime, String name) {
        this();
        this.id = id;
        this.runtime = runtime;
        this.title = name;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public long getBudget() { return budget; }
    public void setBudget(long budget) { this.budget = budget; }

    public long getRevenue() { return revenue; }
    public void setRevenue(long revenue) { this.revenue = revenue; }

    public List<Genre> getGenres() { return Collections.unmodifiableList(genres); }
    public void setGenres(List<Genre> genres) { this.genres = (genres != null) ? new ArrayList<>(genres) : new ArrayList<>(); }

    public List<ProductionCompany> getProductionCompanies() { return Collections.unmodifiableList(productionCompanies); }
    public void setProductionCompanies(List<ProductionCompany> productionCompanies) { this.productionCompanies = (productionCompanies != null) ? new ArrayList<>(productionCompanies) : new ArrayList<>(); }

    public List<ProductionCountry> getProductionCountries() { return Collections.unmodifiableList(productionCountries); }
    public void setProductionCountries(List<ProductionCountry> productionCountries) { this.productionCountries = (productionCountries != null) ? new ArrayList<>(productionCountries) : new ArrayList<>(); }

    public List<SpokenLanguage> getSpokenLanguages() { return Collections.unmodifiableList(spokenLanguages); }
    public void setSpokenLanguages(List<SpokenLanguage> spokenLanguages) { this.spokenLanguages = (spokenLanguages != null) ? new ArrayList<>(spokenLanguages) : new ArrayList<>(); }


    public String getFormattedRuntime() {
        if (runtime <= 0) {
            return "N/A";
        }
        int hours = runtime / 60;
        int minutes = runtime % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public boolean hasGenre(String genreName) {
        if (genreName == null || genres.isEmpty()) {
            return false;
        }
        for (Genre genre : genres) {
            if (genre.getName().equalsIgnoreCase(genreName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReleased() {
        return "Released".equalsIgnoreCase(status);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieModel movieModel = (MovieModel) o;
        return id == movieModel.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MovieModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", overview='" + (overview != null && overview.length() > 50 ? overview.substring(0, 50) + "..." : overview) + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", runtime=" + runtime +
                ", voteAverage=" + voteAverage +
                ", budget=" + budget +
                ", revenue=" + revenue +
                ", genres=" + genres.size() + " genres" +
                '}';
    }


    public static class Genre implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String name;
        public Genre() { /* Default constructor */ }
        public int getId() { return id; }
        public String getName() { return name; }
        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        @Override
        public String toString() { return "Genre{" + "id=" + id + NAME_FIELD_PREFIX + name + '\'' + '}'; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Genre genre = (Genre) o;
            return id == genre.id;
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    public static class ProductionCompany implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        @SerializedName("logo_path")
        private String logoPath;
        private String name;
        @SerializedName("origin_country")
        private String originCountry;
        public ProductionCompany() { /* Default constructor */ }
        public int getId() { return id; }
        public String getLogoPath() { return logoPath; }
        public String getName() { return name; }
        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public String getOriginCountry() { return originCountry; }
        public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
        @Override
        public String toString() { return "ProductionCompany{" + "id=" + id + NAME_FIELD_PREFIX + name + '\'' + '}'; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductionCompany that = (ProductionCompany) o;
            return id == that.id;
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    public static class ProductionCountry implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("iso_3166_1")
        private String iso31661;
        private String name;
        public ProductionCountry() { /* Default constructor */ }
        public String getIso31661() { return iso31661; }
        public String getName() { return name; }
        public void setIso31661(String iso31661) { this.iso31661 = iso31661; }
        public void setName(String name) { this.name = name; }
        @Override
        public String toString() { return "ProductionCountry{" + "iso31661='" + iso31661 + '\'' + NAME_FIELD_PREFIX + name + '\'' + '}'; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductionCountry that = (ProductionCountry) o;
            return Objects.equals(iso31661, that.iso31661);
        }
        @Override
        public int hashCode() { return Objects.hash(iso31661); }
    }

    public static class SpokenLanguage implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("english_name")
        private String englishName;
        @SerializedName("iso_639_1")
        private String iso6391;
        private String name;
        public SpokenLanguage() { /* Default constructor */ }
        public String getEnglishName() { return englishName; }
        public String getIso6391() { return iso6391; }
        public String getName() { return name; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }
        public void setIso6391(String iso6391) { this.iso6391 = iso6391; }
        public void setName(String name) { this.name = name; }
        @Override
        public String toString() { return "SpokenLanguage{" + "iso6391='" + iso6391 + '\'' + NAME_FIELD_PREFIX + name + '\'' + '}'; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SpokenLanguage that = (SpokenLanguage) o;
            return Objects.equals(iso6391, that.iso6391);
        }
        @Override
        public int hashCode() { return Objects.hash(iso6391); }
    }
}