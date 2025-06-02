package com.example.movieapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Data class representing a summary of movie search results.
 *
 * This class models the basic information returned from a movie search API or database query.
 * It includes core identifying details such as the movie's title, release year, and IMDb ID,
 * along with optional descriptive fields (with default values of "N/A") for more detailed metadata.
 *
 * @property title The title of the movie.
 * @property year The year the movie was released.
 * @property imdbID The IMDb identifier for the movie.
 * @property rated The movie's rating (e.g., PG-13, R). Defaults to "N/A".
 * @property released The official release date of the movie. Defaults to "N/A".
 * @property runtime The duration of the movie (e.g., "142 min"). Defaults to "N/A".
 * @property genre A comma-separated list of genres associated with the movie. Defaults to "N/A".
 * @property director The name(s) of the movie's director(s). Defaults to "N/A".
 * @property actors The main cast of the movie. Defaults to "N/A".
 * @property poster The URL of the movie poster image. Defaults to "N/A".
 */
data class MovieSearchResult(
    val title: String,
    val year: String,
    val imdbID: String,
    val rated: String = "N/A",
    val released: String = "N/A",
    val runtime: String = "N/A",
    val genre: String = "N/A",
    val director: String = "N/A",
    val actors: String = "N/A",
    val poster: String = "N/A"
)

class SearchAll : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AllSearch()
                }
            }
        }
    }
}

/**
 * Composable function that provides a user interface for searching movies by title.
 *
 * This screen allows the user to input a partial movie title and triggers a search
 * for matching movie records. The search results are displayed in a scrollable list
 * with basic metadata for each found movie. It also handles loading indicators,
 * error messages, and navigation back to the main screen.
 *
 * Features:
 * - Outlined text field to accept partial title input.
 * - Search button with validation and feedback (via Toast).
 * - Progress indicator while searching.
 * - Dynamic result display using LazyColumn with individual Cards.
 * - Error handling with visual feedback.
 * - Navigation back to the MainActivity using an icon button.
 *
 * State Management:
 * - `textFieldValue`: Stores the user's input.
 * - `foundMovies`: List of matching [MovieSearchResult]s.
 * - `isSearching`: Boolean indicating if the search is in progress.
 * - `errorMessage`: Displays errors from the search function.
 *
 * Uses:
 * - `searchMoviesEnhanced`: A callback-based movie search function to fetch data.
 * - `rememberSaveable` to retain state across recompositions.
 *
 * Requires:
 * - A working implementation of `searchMoviesEnhanced` function with a proper callback.
 * - Android context for navigation and showing Toasts.
 *
 * @see MovieSearchResult
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSearch() {
    var textFieldValue by rememberSaveable { mutableStateOf("") }
    var foundMovies by rememberSaveable { mutableStateOf<List<MovieSearchResult>>(emptyList()) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        //modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Title Search",
                    style = MaterialTheme.typography.headlineMedium
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

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                },
                singleLine = true,
                label = { Text("Enter part of a movie title") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (textFieldValue.isNotBlank()) {
                        isSearching = true
                        errorMessage = ""
                        foundMovies = emptyList()

                        Toast.makeText(
                            context,
                            "Searching for titles containing: $textFieldValue",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Use enhanced search logic from friend's code
                        searchMoviesEnhanced(context, textFieldValue) { result, error ->
                            foundMovies = result
                            errorMessage = error
                            isSearching = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter part of a movie title",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isSearching
            ) {
                Text(if (isSearching) "Searching..." else "Search")
            }

            // Progress indicator
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Error message display
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (foundMovies.isNotEmpty()) {
                Text(
                    text = "Found ${foundMovies.size} movie(s):",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 700.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(foundMovies) { movie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Year: ${movie.year}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (movie.rated != "N/A") {
                                    Text(
                                        text = "Rated: ${movie.rated}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (movie.released != "N/A") {
                                    Text(
                                        text = "Released: ${movie.released}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (movie.runtime != "N/A") {
                                    Text(
                                        text = "Runtime: ${movie.runtime}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (movie.genre != "N/A") {
                                    Text(
                                        text = "Genre: ${movie.genre}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (movie.director != "N/A") {
                                    Text(
                                        text = "Director: ${movie.director}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (movie.actors != "N/A") {
                                    Text(
                                        text = "Actors: ${movie.actors}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
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
 * Enhanced movie search function that attempts multiple strategies to find matching movie titles.
 *
 * This function performs a network search using the OMDb API based on the user's query.
 * It uses a coroutine launched on the IO dispatcher to perform network operations and
 * posts results back to the main thread via a callback.
 *
 * Search Strategy:
 * - First attempts a direct partial title match.
 * - If no results and the query is at least 3 characters, retries with a wildcard (`*`) appended.
 * - If still no results, retries with common prepended keywords like "the", "a", and empty string to increase match chances.
 *
 * @param context The Android context used for logging or potential future UI interactions.
 * @param searchQuery The user-entered search term to find movie titles.
 * @param onResult Callback function returning a list of [MovieSearchResult] and an error message (empty if successful).
 *
 * @see MovieSearchResult
 */
fun searchMoviesEnhanced(context: Context, searchQuery: String, onResult: (List<MovieSearchResult>, String) -> Unit) {
    val apiKey = "e4b4398" // Keep your original API key

    CoroutineScope(Dispatchers.IO).launch {
        try {
            var foundMovies = emptyList<MovieSearchResult>()

            // First try normal search
            foundMovies = searchMoviesByPartialTitle(searchQuery, apiKey)

            // If no results and query is at least 3 chars, try with wildcard
            if (foundMovies.isEmpty() && searchQuery.length >= 3) {
                foundMovies = searchMoviesByPartialTitle("${searchQuery}*", apiKey)

                // If still no results, try with common movie keywords
                if (foundMovies.isEmpty()) {
                    val commonWords = listOf("the", "a", "")
                    for (word in commonWords) {
                        if (foundMovies.isEmpty()) {
                            val term = if (word.isEmpty()) searchQuery else "$word $searchQuery"
                            foundMovies = searchMoviesByPartialTitle(term, apiKey)
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (foundMovies.isEmpty()) {
                    onResult(emptyList(), "No movies found matching: $searchQuery")
                } else {
                    onResult(foundMovies, "")
                }
            }
        } catch (e: Exception) {
            Log.e("MovieApp", "Error searching movies: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onResult(emptyList(), "Error: ${e.message ?: "Unknown error occurred"}")
            }
        }
    }
}

/**
 * Searches the OMDb API for movies that partially match the provided title.
 *
 * This function performs a network call to the OMDb API using the `s=` query parameter for
 * searching titles. It filters the results client-side to ensure the title contains the
 * lowercase form of the `partialTitle` (excluding wildcards), then retrieves detailed
 * information for each matching movie using the `i=` query with IMDb ID.
 *
 * The function runs on the IO dispatcher and returns a list of [MovieSearchResult] objects
 * containing expanded details like rating, runtime, genre, and cast.
 *
 * @param partialTitle The partial movie title to search for. Wildcards (`*`) are allowed for custom client-side filtering.
 * @param apiKey The OMDb API key used to authenticate requests.
 * @return A list of [MovieSearchResult] with enriched movie data, or an empty list if none are found or an error occurs.
 *
 * Error Handling:
 * - Logs any exceptions using `Log.e`.
 * - If an exception or API error occurs (e.g., invalid key, no internet), returns an empty list.
 *
 * @see MovieSearchResult
 */
suspend fun searchMoviesByPartialTitle(partialTitle: String, apiKey: String): List<MovieSearchResult> {
    return withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(partialTitle.trim(), StandardCharsets.UTF_8.toString())
            val url = URL("https://www.omdbapi.com/?s=$encodedQuery&apikey=$apiKey")

            val response = url.readText()
            Log.d("MovieApp", "OMDB API Response: $response")

            val jsonObject = JSONObject(response)
            val success = jsonObject.optString("Response", "False") == "True"

            if (!success) {
                return@withContext emptyList<MovieSearchResult>()
            }

            val searchResults = jsonObject.getJSONArray("Search")
            if (searchResults.length() == 0) {
                return@withContext emptyList<MovieSearchResult>()
            }

            // Process each result
            val matchingMovies = mutableListOf<MovieSearchResult>()
            val query = partialTitle.lowercase().replace("*", "")

            for (i in 0 until searchResults.length()) {
                val movie = searchResults.getJSONObject(i)
                val title = movie.optString("Title", "")

                if (title.lowercase().contains(query)) {
                    // Get more details for this movie
                    val imdbID = movie.getString("imdbID")
                    val detailUrl = URL("https://www.omdbapi.com/?i=$imdbID&apikey=$apiKey")
                    val detailResponse = detailUrl.readText()
                    val detailObject = JSONObject(detailResponse)

                    matchingMovies.add(
                        MovieSearchResult(
                            title = detailObject.optString("Title", "N/A"),
                            year = detailObject.optString("Year", "N/A"),
                            imdbID = detailObject.optString("imdbID", "N/A"),
                            rated = detailObject.optString("Rated", "N/A"),
                            released = detailObject.optString("Released", "N/A"),
                            runtime = detailObject.optString("Runtime", "N/A"),
                            genre = detailObject.optString("Genre", "N/A"),
                            director = detailObject.optString("Director", "N/A"),
                            actors = detailObject.optString("Actors", "N/A")
                        )
                    )
                }
            }

            return@withContext matchingMovies
        } catch (e: Exception) {
            Log.e("MovieApp", "Error searching movies: ${e.message}", e)
            return@withContext emptyList<MovieSearchResult>()
        }
    }
}