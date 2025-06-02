package com.example.movieapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


/**
 * Data Access Object (DAO) for interacting with the `movies` table in the Room database.
 *
 * This interface provides methods to interact with the `Movie` entity in the database. It defines
 * several queries for inserting, fetching, and searching movie data.
 *
 * The `@Dao` annotation marks this interface as a DAO, and it allows Room to generate the necessary
 * code to interact with the database.
 */
@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    @Query("SELECT * FROM movies WHERE imdbID = :id")
    suspend fun getMovieById(id: String): Movie?

    @Query("SELECT * FROM movies WHERE actors LIKE :actorName ORDER BY year DESC")
    suspend fun findMoviesByActor(actorName: String): List<Movie>
}