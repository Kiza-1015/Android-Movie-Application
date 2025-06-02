package com.example.movieapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a movie entity in the database.
 *
 * This class defines the structure of the `movies` table in the Room database. Each instance of
 * this class corresponds to a row in the table, with fields that represent various properties
 * of a movie such as title, release year, genre, director, and other relevant details.
 *
 * @property id The unique identifier for each movie in the database, automatically generated.
 * @property title The title of the movie.
 * @property year The release year of the movie.
 * @property rated The movie's rating (e.g., PG, R).
 * @property released The release date of the movie.
 * @property runtime The total runtime of the movie.
 * @property genre The genre(s) of the movie (e.g., Action, Comedy).
 * @property director The director of the movie.
 * @property writer The writer(s) of the movie.
 * @property actors The list of actors featured in the movie.
 * @property plot A brief plot summary of the movie.
 * @property language The language(s) in which the movie is available.
 * @property country The country where the movie was produced.
 * @property awards The awards won or nominated for by the movie.
 * @property poster The URL of the movie's poster image.
 * @property ratings The ratings of the movie (e.g., IMDb rating, Rotten Tomatoes score).
 * @property imdbID The unique IMDb ID for the movie.
 * @property type The type of the movie (e.g., Movie, Series, Documentary).
 */
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
    val language: String,
    val country: String,
    val awards: String,
    val poster: String,
    val ratings: String,
    val imdbID: String,
    val type: String
)