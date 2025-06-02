package com.example.movieapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MovieSearch : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MovieSearchScreen()
                }
            }
        }
    }
}

/**
 * Represents a detailed movie item retrieved from the OMDb API, used for storing
 * both summary and extended information about a specific movie.
 *
 * This model is typically used to display movie details in a list or detailed view,
 * while also optionally tracking user interaction and storing raw API data for future use.
 *
 * @property imdbID Unique IMDb identifier for the movie (e.g., "tt0133093").
 * @property title The title of the movie (e.g., "The Matrix").
 * @property year The release year of the movie (e.g., "1999").
 * @property rated The content rating of the movie (e.g., "R", "PG-13").
 * @property released The official release date (e.g., "31 Mar 1999").
 * @property runtime Duration of the movie (e.g., "136 min").
 * @property genre A comma-separated list of genres (e.g., "Action, Sci-Fi").
 * @property director Name(s) of the director(s).
 * @property writer Name(s) of the writer(s).
 * @property actors A list of main actors in the film.
 * @property plot A short description or synopsis of the movie.
 * @property isSelected A UI-related flag to indicate if the item is selected (e.g., in a list).
 * @property fullDetails Optional raw JSON object containing the full API response for advanced use or caching.
 *
 * @see <a href="https://www.omdbapi.com/">OMDb API Documentation</a>
 */data class MovieSearchItem(
    val imdbID: String,
    val title: String,
    val year: String,
    val rated: String,
    val released: String,
    val runtime: String,
    val genre: String,
    val director: String,
    val writer: String,
    val actors: String,
    val plot: String,
    val isSelected: Boolean = false,
    val fullDetails: JSONObject? = null
)

/**
 * A composable function that displays a movie search screen where users can search for movies by title,
 * select multiple movies, and save them to the local database.
 *
 * This screen includes:
 * - A text field for entering the movie title to search for.
 * - A search button that triggers a network call to search for movies using the entered title.
 * - A checkbox for selecting/deselecting all movies in the search results.
 * - A button to save the selected movies to the local database.
 * - A list of movie results with the option to toggle the selection of each movie.
 * - A loading spinner displayed while waiting for the API call to complete.
 * - Error messages displayed when an error occurs during the API call or when no movies are selected for saving.
 * - Pagination functionality to load more results if there are more than the initial results.
 *
 * @param context The context used for initializing the database and navigation.
 * @param scope The coroutine scope used for launching asynchronous tasks.
 * @param db The instance of the MovieDatabase used to interact with the local database.
 * @param movieDao The DAO for interacting with the movies in the database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieSearchScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { MovieDatabase.getDatabase(context) }
    val movieDao = remember { db.movieDao() }

    // State variables for UI and API handling
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var searchResults by rememberSaveable  { mutableStateOf<List<MovieSearchItem>>(emptyList()) }
    var selectedMovies by rememberSaveable  { mutableStateOf<Set<String>>(emptySet()) }
    var currentPage by rememberSaveable { mutableIntStateOf(1) }
    var totalResults by rememberSaveable { mutableIntStateOf(0) }
    var selectAllChecked by remember { mutableStateOf(false) }

    val apiKey = "111180dd" // Using the same API key

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movie Search",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Enter movie title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    isLoading = true
                                    errorMessage = ""
                                    searchResults = emptyList()
                                    selectedMovies = emptySet()
                                    currentPage = 1

                                    scope.launch {
                                        try {
                                            val results = searchMovies(searchQuery, apiKey, currentPage)
                                            searchResults = results.first
                                            totalResults = results.second
                                        } catch (e: Exception) {
                                            errorMessage = "Error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage = "Please enter a movie title"
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Black
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                isLoading = true
                                errorMessage = ""
                                searchResults = emptyList()
                                selectedMovies = emptySet()
                                currentPage = 1

                                scope.launch {
                                    try {
                                        val results = searchMovies(searchQuery, apiKey, currentPage)
                                        searchResults = results.first
                                        totalResults = results.second
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = "Please enter a movie title"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(end = 8.dp)) {
                        Text("Search Movies")
                    }

                    Button(
                        onClick = {
                            if (selectedMovies.isNotEmpty()) {
                                scope.launch {
                                    try {
                                        val moviesToSave = mutableListOf<Movie>()

                                        // For each selected movie, fetch full details and convert to Movie entity
                                        for (movieId in selectedMovies) {
                                            val result = fetchMovieById(movieId, apiKey)
                                            if (result.isNotEmpty()) {
                                                val movie = parseMovieJson(result)
                                                moviesToSave.add(movie)
                                            }
                                        }

                                        // Save all movies to database
                                        withContext(Dispatchers.IO) {
                                            movieDao.insertAll(moviesToSave)
                                        }

                                        errorMessage = "${moviesToSave.size} movies saved to database successfully!"
                                        selectedMovies = emptySet()
                                    } catch (e: Exception) {
                                        errorMessage = "Error saving to database: ${e.message}"
                                        Log.e("MovieApp", "Database error", e)
                                    }
                                }
                            } else {
                                errorMessage = "Please select at least one movie to save"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(start = 8.dp)) {
                        Text("Save Selected (${selectedMovies.size})")
                    }
                    Checkbox(
                        checked = selectAllChecked,
                        onCheckedChange = { isChecked ->
                            selectAllChecked = isChecked
                            selectedMovies = if (isChecked) {
                                // Select all movies
                                searchResults.map { it.imdbID }.toSet()
                            } else {
                                // Deselect all movies
                                emptySet()
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF1E88E5),
                            uncheckedColor = Color.Black
                        )
                    )
                    Text(
                        text = "Select \nAll \nMovies",
                        modifier = Modifier
                            .clickable {
                                selectAllChecked = !selectAllChecked
                                selectedMovies = if (selectAllChecked) {
                                    searchResults.map { it.imdbID }.toSet()
                                } else {
                                    emptySet()
                                }
                            }
                            .padding(start = 8.dp),
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = if (errorMessage.startsWith("Error")) Color.Red else Color.Green,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "Found $totalResults results. Showing ${searchResults.size}",
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(searchResults) { movie ->
                            MovieResultItem(
                                movie = movie,
                                isSelected = selectedMovies.contains(movie.imdbID),
                                onToggleSelection = {
                                    selectedMovies = if (selectedMovies.contains(movie.imdbID)) {
                                        selectedMovies - movie.imdbID
                                    } else {
                                        selectedMovies + movie.imdbID
                                    }
                                }
                            )
                        }

                        // Load more functionality
                        item {
                            if (searchResults.size < totalResults) {
                                Button(
                                    onClick = {
                                        if (!isLoading) {
                                            isLoading = true
                                            currentPage++

                                            scope.launch {
                                                try {
                                                    val moreResults = searchMovies(searchQuery, apiKey, currentPage)
                                                    searchResults = searchResults + moreResults.first
                                                } catch (e: Exception) {
                                                    errorMessage = "Error loading more results: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                                ) {
                                    Text("Load More Results")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A composable function that displays a movie result item with a checkbox to toggle its selection.
 * The item displays various details about the movie, including its title, year, director, writer, actors, and plot.
 *
 * This component is used within a list to represent individual search results in the movie search screen. It allows the user to
 * select or deselect a movie, and visually highlights the selected state by changing the card color.
 *
 * @param movie The movie data to display, containing information like title, year, genre, and more.
 * @param isSelected A boolean value that determines if the movie is selected or not.
 * @param onToggleSelection A lambda function that toggles the selection state when the checkbox or card is clicked.
 */
@Composable
fun MovieResultItem(
    movie: MovieSearchItem,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleSelection() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF1E88E5).copy(alpha = 0.3f)
            else
                Color.Black.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF1E88E5),
                    uncheckedColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movie.title,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "${movie.year} • ${movie.released}",
                    color = Color.Black.copy(alpha = 0.9f),
                    fontSize = 17.sp
                )
                Text(
                    text = "Rated: ${movie.rated} • ${movie.genre} • Runtime: ${movie.runtime}",
                    color = Color.Black.copy(alpha = 0.9f)
                )
                Text(
                    text = "Director: ${movie.director}",
                    color = Color.Black.copy(alpha = 0.9f)
                )
                Text(
                    text = "Writer: ${movie.writer}",
                    color = Color.Black.copy(alpha = 0.9f)
                )
                Text(
                    text = "Actor: ${movie.actors}",
                    color = Color.Black.copy(alpha = 0.9f)
                )
                Text(
                    text = "Plot: ${movie.plot}",
                    color = Color.Black.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Fetches a list of movie search results based on a query and page number.
 * It performs a network request to the OMDb API, retrieves the search results, and returns a list of `MovieSearchItem` objects along with the total number of results.
 *
 * @param query The search query (e.g., movie title).
 * @param apiKey The OMDb API key used for authentication.
 * @param page The page number for paginated results.
 * @return A pair containing a list of `MovieSearchItem` objects and the total number of results.
 */
suspend fun searchMovies(query: String, apiKey: String, page: Int): Pair<List<MovieSearchItem>, Int> {
    return withContext(Dispatchers.IO) {
        val urlString = "https://www.omdbapi.com/?s=${query.replace(" ", "+")}&apikey=$apiKey&page=$page"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val jsonResponse = response.toString()
            val jsonObject = JSONObject(jsonResponse)

            if (jsonObject.has("Error")) {
                return@withContext Pair(emptyList<MovieSearchItem>(), 0)
            }

            val totalResults = jsonObject.optString("totalResults", "0").toIntOrNull() ?: 0
            val searchArray = jsonObject.getJSONArray("Search")
            val results = mutableListOf<MovieSearchItem>()

            for (i in 0 until searchArray.length()) {
                val movie = searchArray.getJSONObject(i)
                val imdbID = movie.optString("imdbID", "")

                // Fetch detailed info for each movie
                val detailedMovie = fetchDetailedMovieInfo(imdbID, apiKey)

                results.add(detailedMovie)
            }

            return@withContext Pair(results, totalResults)
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Fetches detailed movie information for a specific IMDb ID.
 * This function performs a network request to the OMDb API to get more detailed information for a single movie based on its IMDb ID.
 *
 * @param imdbId The IMDb ID of the movie.
 * @param apiKey The OMDb API key used for authentication.
 * @return A `MovieSearchItem` containing detailed information about the movie.
 */
suspend fun fetchDetailedMovieInfo(imdbId: String, apiKey: String): MovieSearchItem {
    return withContext(Dispatchers.IO) {
        val urlString = "https://www.omdbapi.com/?i=$imdbId&apikey=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val jsonString = response.toString()
            val json = JSONObject(jsonString)

            MovieSearchItem(
                imdbID = json.optString("imdbID", ""),
                title = json.optString("Title", ""),
                year = json.optString("Year", ""),
                rated = json.optString("Rated", "N/A"),
                released = json.optString("Released", "N/A"),
                runtime = json.optString("Runtime", "N/A"),
                genre = json.optString("Genre", "N/A"),
                director = json.optString("Director", "N/A"),
                writer = json.optString("Writer", "N/A"),
                actors = json.optString("Actors", "N/A"),
                plot = json.optString("Plot", "N/A"),
                fullDetails = json
            )
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Fetches detailed movie information for a specific IMDb ID as a raw JSON string.
 * This function performs a network request to the OMDb API and returns the response in JSON format.
 *
 * @param imdbId The IMDb ID of the movie.
 * @param apiKey The OMDb API key used for authentication.
 * @return A JSON string containing detailed movie information.
 */
suspend fun fetchMovieById(imdbId: String, apiKey: String): String {
    return withContext(Dispatchers.IO) {
        val urlString = "https://www.omdbapi.com/?i=$imdbId&apikey=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            response.toString()
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Converts a JSON string into a `Movie` data object.
 * This function parses the JSON response from the OMDb API into a `Movie` object, mapping the relevant fields from the JSON string to the data class properties.
 *
 * @param jsonString The JSON string containing movie details from the OMDb API.
 * @return A `Movie` object populated with the parsed data.
 */
fun parseMovieJson(jsonString: String): Movie {
    val json = JSONObject(jsonString)

    return Movie(
        id = 0, // Auto-generate ID
        title = json.optString("Title", ""),
        year = json.optString("Year", ""),
        rated = json.optString("Rated", ""),
        released = json.optString("Released", ""),
        runtime = json.optString("Runtime", ""),
        genre = json.optString("Genre", ""),
        director = json.optString("Director", ""),
        writer = json.optString("Writer", ""),
        actors = json.optString("Actors", ""),
        plot = json.optString("Plot", ""),
        language = json.optString("Language", ""),
        country = json.optString("Country", ""),
        awards = json.optString("Awards", ""),
        poster = json.optString("Poster", ""),
        ratings = json.optString("imdbRating", ""),
        imdbID = json.optString("imdbID", "tt0000000"),
        type = json.optString("Type", "movie")
    )
}