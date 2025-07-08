package ispw.project.movietime.bean;

import ispw.project.movietime.model.MovieModel; // Import your model
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects; // Don't forget to import Objects for equals/hashCode
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

// Import JavaFX properties for reactive UI (highly recommended for JavaFX)
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MovieBean {

    private static final Logger logger = Logger.getLogger(MovieBean.class.getName());

    private final IntegerProperty id;

    private final StringProperty title;
    private final StringProperty originalTitle;
    private final StringProperty originalLanguage;
    private final StringProperty overview;
    private final StringProperty formattedReleaseDate;
    private final StringProperty voteAverageDisplay;
    private final StringProperty voteCountDisplay;
    private final StringProperty runtimeDisplay;
    private final StringProperty popularityDisplay;
    private final StringProperty status;
    private final StringProperty tagline;
    private final StringProperty imdbId;
    private final StringProperty budgetDisplay;
    private final StringProperty revenueDisplay;
    private final StringProperty genresDisplay;
    private final StringProperty productionCompaniesDisplay;
    private final StringProperty productionCountriesDisplay;
    private final StringProperty spokenLanguagesDisplay;

    private final StringProperty fullPosterUrl;
    private final StringProperty fullBackdropUrl;

    public MovieBean() {
        this.id = new SimpleIntegerProperty();
        this.title = new SimpleStringProperty();
        this.originalTitle = new SimpleStringProperty();
        this.originalLanguage = new SimpleStringProperty();
        this.overview = new SimpleStringProperty();
        this.formattedReleaseDate = new SimpleStringProperty();
        this.voteAverageDisplay = new SimpleStringProperty();
        this.voteCountDisplay = new SimpleStringProperty();
        this.runtimeDisplay = new SimpleStringProperty();
        this.popularityDisplay = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.tagline = new SimpleStringProperty();
        this.imdbId = new SimpleStringProperty();
        this.budgetDisplay = new SimpleStringProperty();
        this.revenueDisplay = new SimpleStringProperty();
        this.genresDisplay = new SimpleStringProperty();
        this.productionCompaniesDisplay = new SimpleStringProperty();
        this.productionCountriesDisplay = new SimpleStringProperty();
        this.spokenLanguagesDisplay = new SimpleStringProperty();
        this.fullPosterUrl = new SimpleStringProperty();
        this.fullBackdropUrl = new SimpleStringProperty();
    }

    public MovieBean(MovieModel model) {
        this(); // Call the default constructor to initialize all properties first.
        if (model != null) {
            this.id.set(model.getId());
            this.title.set(model.getTitle());
            this.originalTitle.set(model.getOriginalTitle());
            this.originalLanguage.set(model.getOriginalLanguage());
            this.overview.set(model.getOverview() != null ? model.getOverview() : "No overview available.");
            this.status.set(model.getStatus());
            this.tagline.set(model.getTagline());
            this.imdbId.set(model.getImdbId());

            this.formattedReleaseDate.set(formatDate(model.getReleaseDate()));
            this.voteAverageDisplay.set(String.format("%.1f", model.getVoteAverage())); // Format to one decimal place
            this.voteCountDisplay.set(String.valueOf(model.getVoteCount()));
            this.runtimeDisplay.set(model.getRuntime() > 0 ? model.getRuntime() + " minutes" : "N/A");
            this.popularityDisplay.set(String.format("%.2f", model.getPopularity())); // Format to two decimal places
            this.budgetDisplay.set(formatCurrency(model.getBudget()));
            this.revenueDisplay.set(formatCurrency(model.getRevenue()));

            this.genresDisplay.set(formatListNames(model.getGenres(), MovieModel.Genre::getName));
            this.productionCompaniesDisplay.set(formatListNames(model.getProductionCompanies(), pc -> pc.getName()));
            this.productionCountriesDisplay.set(formatListNames(model.getProductionCountries(), pc -> pc.getName()));
            this.spokenLanguagesDisplay.set(formatListNames(model.getSpokenLanguages(), sl -> sl.getName())); // Or getEnglishName()

            String baseUrlW500 = "https://image.tmdb.org/t/p/w500";
            String baseUrlW1280  = "https://image.tmdb.org/t/p/w1280";

            this.fullPosterUrl.set(
                    (model.getPosterPath() != null && !model.getPosterPath().isEmpty()) ?
                            baseUrlW500 + model.getPosterPath() : "/path/to/default_no_poster.png"
            );
            this.fullBackdropUrl.set(
                    (model.getBackdropPath() != null && !model.getBackdropPath().isEmpty()) ?
                            baseUrlW1280  + model.getBackdropPath() : "/path/to/default_no_backdrop.png"
            );
        }
    }

    private String formatDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            } catch (java.time.format.DateTimeParseException e) {
                logger.log(Level.SEVERE, e, () -> "Error parsing release date: " + dateString + " - " + e.getMessage());
                return "N/A";
            }
        }
        return "N/A";
    }

    private String formatCurrency(long value) {
        if (value <= 0) return "N/A";
        return String.format("$%,d", value);
    }

    private <T> String formatListNames(List<T> list, java.util.function.Function<T, String> nameExtractor) {
        if (list == null || list.isEmpty()) {
            return "N/A";
        }
        return list.stream()
                .map(nameExtractor)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty originalTitleProperty() { return originalTitle; }
    public StringProperty originalLanguageProperty() { return originalLanguage; }
    public StringProperty overviewProperty() { return overview; }
    public StringProperty formattedReleaseDateProperty() { return formattedReleaseDate; }
    public StringProperty voteAverageDisplayProperty() { return voteAverageDisplay; }
    public StringProperty voteCountDisplayProperty() { return voteCountDisplay; }
    public StringProperty runtimeDisplayProperty() { return runtimeDisplay; }
    public StringProperty popularityDisplayProperty() { return popularityDisplay; }
    public StringProperty statusProperty() { return status; }
    public StringProperty taglineProperty() { return tagline; }
    public StringProperty imdbIdProperty() { return imdbId; }
    public StringProperty budgetDisplayProperty() { return budgetDisplay; }
    public StringProperty revenueDisplayProperty() { return revenueDisplay; }
    public StringProperty genresDisplayProperty() { return genresDisplay; }
    public StringProperty productionCompaniesDisplayProperty() { return productionCompaniesDisplay; }
    public StringProperty productionCountriesDisplayProperty() { return productionCountriesDisplay; }
    public StringProperty spokenLanguagesDisplayProperty() { return spokenLanguagesDisplay; }
    public StringProperty fullPosterUrlProperty() { return fullPosterUrl; }
    public StringProperty fullBackdropUrlProperty() { return fullBackdropUrl; } // If you need it for another image view

    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getOriginalTitle() { return originalTitle.get(); }
    public String getOriginalLanguage() { return originalLanguage.get(); }
    public String getOverview() { return overview.get(); }
    public String getFormattedReleaseDate() { return formattedReleaseDate.get(); }
    public String getVoteAverageDisplay() { return voteAverageDisplay.get(); }
    public String getVoteCountDisplay() { return voteCountDisplay.get(); }
    public String getRuntimeDisplay() { return runtimeDisplay.get(); }
    public String getPopularityDisplay() { return popularityDisplay.get(); }
    public String getStatus() { return status.get(); }
    public String getTagline() { return tagline.get(); }
    public String getImdbId() { return imdbId.get(); }
    public String getBudgetDisplay() { return budgetDisplay.get(); }
    public String getRevenueDisplay() { return revenueDisplay.get(); }
    public String getGenresDisplay() { return genresDisplay.get(); }
    public String getProductionCompaniesDisplay() { return productionCompaniesDisplay.get(); }
    public String getProductionCountriesDisplay() { return productionCountriesDisplay.get(); }
    public String getSpokenLanguagesDisplay() { return spokenLanguagesDisplay.get(); }
    public String getFullPosterUrl() { return fullPosterUrl.get(); }
    public String getFullBackdropUrl() { return fullBackdropUrl.get(); }

    public void setId(int id) {
        this.id.set(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieBean movieBean = (MovieBean) o;
        return id.get() == movieBean.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "MovieBean{" +
                "id=" + id.get() +
                ", title='" + title.get() + '\'' +
                ", formattedReleaseDate='" + formattedReleaseDate.get() + '\'' +
                ", overview='" + overview.get() + '\'' +
                '}';
    }
}