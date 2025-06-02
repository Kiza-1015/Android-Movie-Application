package com.example.movieapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movieapplication.ui.theme.MovieApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MovieApplicationTheme {
                ResponsiveStartScreen(applicationContext)
            }
        }
    }
}

/**
 * Composable function that displays the responsive start screen for the Movie Search Application.
 * It uses a [Scaffold] layout with a top app bar and switches between landscape and portrait
 * layouts based on the current device orientation.
 *
 * @param appContext The application context, passed to child composables for operations that require context.
 *
 * This screen adapts to orientation changes:
 * - In **landscape mode**, [LandscapeLayout] is displayed.
 * - In **portrait mode**, [PortraitLayout] is displayed.
 *
 * The top app bar includes the app title styled with `headlineMedium`, centered across the top.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveStartScreen(appContext: android.content.Context) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movie Search Application",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLandscape) {
                LandscapeLayout(appContext)
            } else {
                PortraitLayout(appContext)
            }
        }
    }
}

/**
 * Composable function that defines the UI layout for portrait orientation in the
 * Movie Search Application.
 *
 * Displays a centered column containing:
 * - An application icon image with rounded corners.
 * - A spacer for visual separation.
 * - Action buttons for user interaction, passed the application context.
 *
 * @param appContext The application context, used for operations in [ActionButtons].
 */
@Composable
fun PortraitLayout(appContext: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.appicon),
            contentDescription = stringResource(id = R.string.app_name),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(32.dp))

        ActionButtons(appContext, Modifier.width(250.dp))
    }
}

/**
 * Composable function that defines the UI layout for landscape orientation in the
 * Movie Search Application.
 *
 * Displays a horizontal row containing:
 * - An application icon image with rounded corners.
 * - A spacer for separation.
 * - Action buttons for user interaction, passed the application context.
 *
 * This layout ensures even spacing and centers content vertically across the screen.
 *
 * @param appContext The application context, used by the [ActionButtons] composable.
 */
@Composable
fun LandscapeLayout(appContext: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Image(
            painter = painterResource(id = R.drawable.appicon),
            contentDescription = stringResource(id = R.string.app_name),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(32.dp))

        ActionButtons(appContext, Modifier.width(220.dp))
    }
}

/**
 * Composable function that displays a vertical column of action buttons used in the
 * Movie Search Application.
 *
 * This includes:
 * - A button to add movies to the database via [AddMoviesToDbButton].
 * - A button to navigate to the movie search screen.
 * - A button to navigate to the actor search screen.
 * - A button to navigate to an extended search screen.
 *
 * Each button uses the provided [buttonModifier] for consistent sizing and styling.
 *
 * @param appContext The application context, passed to [AddMoviesToDbButton].
 * @param buttonModifier Modifier applied to all buttons for layout and styling control.
 */
@Composable
fun ActionButtons(appContext: android.content.Context, buttonModifier: Modifier) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AddMoviesToDbButton(appContext, buttonModifier)

        Button(
            onClick = {
                val intent = Intent(context, MovieSearch::class.java)
                context.startActivity(intent)
            },
            modifier = buttonModifier.height(50.dp)
        ) {
            Text("Search for movies", fontSize = 18.sp)
        }

        Button(
            onClick = {
                val intent = Intent(context, ActorSearch::class.java)
                context.startActivity(intent)
            },
            modifier = buttonModifier.height(50.dp)
        ) {
            Text("Search for actors", fontSize = 18.sp)
        }

        Button(
            onClick = {
                val intent = Intent(context, SearchAll::class.java)
                context.startActivity(intent)
            },
            modifier = buttonModifier.height(50.dp)
        ) {
            Text("Search (Extended)", fontSize = 18.sp)
        }
    }
}

/**
 * Composable function that renders a button labeled "Add Movies to DB".
 *
 * When clicked, it performs the following:
 * - Inserts a predefined list of popular movies into the local Room database.
 * - Displays a Toast message indicating success or failure.
 * - Handles coroutine context switching for database operations (using `Dispatchers.IO` and `Dispatchers.Main`).
 *
 * The inserted movies include data for:
 * - *The Shawshank Redemption*
 * - *The Godfather*
 * - *The Dark Knight*
 *
 * Errors during database access are logged and shown to the user via Toast.
 *
 * @param appContext The application context used to access the Room database and show Toast messages.
 * @param modifier Modifier applied to the button for size and layout customization.
 */
@Composable
fun AddMoviesToDbButton(appContext: android.content.Context, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                try {
                    val db = MovieDatabase.getDatabase(appContext)
                    val movieDao = db.movieDao()

                    val movies = listOf(
                        Movie(
                            title = "The Shawshank Redemption",
                            year = "1994",
                            rated = "R",
                            released = "14 Oct 1994",
                            runtime = "142 min",
                            genre = "Drama",
                            director = "Frank Darabont",
                            writer = "Stephen King, Frank Darabont",
                            actors = "Tim Robbins, Morgan Freeman, Bob Gunton",
                            plot = "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                            language = "English",
                            country = "USA",
                            awards = "Nominated for 7 Oscars. 21 wins & 43 nominations total",
                            poster = "https://m.media-amazon.com/images/M/MV5BMDFkYTc0MGEtZmNhMC00ZDIzLWFmNTEtODM1ZmRlYWMwMWFmXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg",
                            ratings = "9.3",
                            imdbID = "tt0111161",
                            type = "movie"
                        ),
                        // Other movies remain the same
                        Movie(
                            title = "The Godfather",
                            year = "1972",
                            rated = "R",
                            released = "24 Mar 1972",
                            runtime = "175 min",
                            genre = "Crime, Drama",
                            director = "Francis Ford Coppola",
                            writer = "Mario Puzo, Francis Ford Coppola",
                            actors = "Marlon Brando, Al Pacino, James Caan",
                            plot = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.",
                            language = "English, Italian, Latin",
                            country = "USA",
                            awards = "Won 3 Oscars. 31 wins & 30 nominations total",
                            poster = "https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg",
                            ratings = "9.2",
                            imdbID = "tt0068646",
                            type = "movie"
                        ),
                        Movie(
                            title = "The Dark Knight",
                            year = "2008",
                            rated = "PG-13",
                            released = "18 Jul 2008",
                            runtime = "152 min",
                            genre = "Action, Crime, Drama",
                            director = "Christopher Nolan",
                            writer = "Jonathan Nolan, Christopher Nolan, David S. Goyer",
                            actors = "Christian Bale, Heath Ledger, Aaron Eckhart",
                            plot = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                            language = "English, Mandarin",
                            country = "USA, UK",
                            awards = "Won 2 Oscars. 159 wins & 163 nominations total",
                            poster = "https://m.media-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX300.jpg",
                            ratings = "9.0",
                            imdbID = "tt0468569",
                            type = "movie"
                        )
                    )

                    withContext(Dispatchers.IO) {
                        movieDao.insertAll(movies)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Movies added to database", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MovieApp", "Database error: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        },
        modifier = modifier.height(50.dp)
    ) {
        Text("Add Movies to DB", fontSize = 18.sp)
    }
}