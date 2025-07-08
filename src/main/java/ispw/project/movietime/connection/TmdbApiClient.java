package ispw.project.movietime.connection;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import ispw.project.movietime.exception.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TmdbApiClient {

    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String SEARCH_MOVIE_URL = "https://api.themoviedb.org/3/search/movie";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static String apiKey;

    static {
        try (InputStream input = TmdbApiClient.class.getClassLoader().getResourceAsStream("api.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                apiKey = prop.getProperty("TMDB_API_KEY");
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    throw new ApiException("TMDB_API_KEY not found or is empty in api.properties! Please ensure it's configured.");
                }
            } else {
                throw new ApiException("api.properties file not found in resources! Please ensure it's in the classpath.");
            }
        } catch (IOException e) {
            throw new ApiException("Failed to load TMDB API key from properties file due to an I/O error.", e);
        }
    }

    private TmdbApiClient() {
        // This constructor is intentionally empty to prevent instantiation.
    }

    private static String executeHttpRequest(String url) throws ApiException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new ApiException(
                        "HTTP error: " + response.code() + ", Body: " + responseBody + " for URL: " + url
                );
            }
            return responseBody;
        } catch (IOException e) {
            throw new ApiException("Network error during HTTP request to " + url + ": " + e.getMessage(), e);
        }
    }

    public static String getMovieJsonById(int movieId) throws ApiException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_MOVIE_URL + movieId).newBuilder();
        urlBuilder.addQueryParameter("api_key", apiKey);
        String url = urlBuilder.build().toString();
        return executeHttpRequest(url);
    }

    public static String searchMoviesJson(String query) throws ApiException {
        return searchMoviesJson(query, 1); // Default to page 1
    }

    public static String searchMoviesJson(String query, int page) throws ApiException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SEARCH_MOVIE_URL).newBuilder();
        urlBuilder.addQueryParameter("api_key", apiKey);
        urlBuilder.addQueryParameter("query", query);
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        String url = urlBuilder.build().toString();
        return executeHttpRequest(url);
    }
}