package com.example.movieapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import kotlinx.coroutines.launch

class ActorSearch : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieApplicationTheme {
                ActorSearchScreen()
            }
        }
    }
}

/**
 * Composable function that displays the "Actor Search" screen.
 * It allows the user to input an actor's name, search for movies featuring that actor,
 * and display the results in a scrollable list.
 *
 * The screen includes a search bar, a button to initiate the search, a loading indicator,
 * and a list of movies (if found) along with error messages if the search fails.
 *
 * @param context The context used to launch an intent for navigating to the main activity.
 * @param coroutineScope A scope for launching coroutines to handle the search operation asynchronously.
 * @param movieDao The data access object (DAO) for interacting with the movie database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorSearchScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = MovieDatabase.getDatabase(context)
    val movieDao = database.movieDao()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var foundMovies by rememberSaveable { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Actor Search",
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

        Column (
            modifier = Modifier
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                label = { Text("Actor Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        // Show searching toast
                        Toast.makeText(
                            context,
                            "Searching for: $searchQuery",
                            Toast.LENGTH_SHORT
                        ).show()

                        isLoading = true
                        errorMessage = ""
                        foundMovies = emptyList()

                        coroutineScope.launch {
                            try {
                                // Use wildcard pattern for more effective substring matching
                                val actorSearchPattern = "%${searchQuery}%"
                                val results = movieDao.findMoviesByActor(actorSearchPattern)

                                foundMovies = results

                                if (results.isEmpty()) {
                                    errorMessage = "No movies found with actor matching: $searchQuery"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error searching: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter an actor name",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Search for actors")
            }

            // Show loading indicator while searching
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Show error message if any
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Display search results
            if (foundMovies.isNotEmpty()) {
                Text(
                    text = "Found ${foundMovies.size} movie(s):",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                // Use a Card with LazyColumn for scrollable results
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(630.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        items(foundMovies) { movie ->
                            MovieItem(movie)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function that displays information about a movie.
 * It presents details such as the movie's title, year, runtime, genre, director, actors, and plot.
 *
 * @param movie The movie whose details will be displayed.
 */
@Composable
fun MovieItem(movie: Movie) {
    Column {
        Text(
            text = "${movie.title} (${movie.year})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Runtime: ${movie.runtime}")
        Text(text = "Genre: ${movie.genre}")
        Text(text = "Director: ${movie.director}")
        Text(text = "Cast: ${movie.actors}")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Plot: ${movie.plot}",
            fontSize = 14.sp
        )
    }
}