package com.example.movieapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for storing movie data.
 *
 * This class is annotated with `@Database` to define the database and specify the entities
 * it will hold (in this case, the `Movie` entity) and the database version. It provides a singleton
 * instance of the database and exposes the `movieDao()` function to access the data access object (DAO)
 * for interacting with the `Movie` table.
 *
 * The companion object ensures that the database instance is created only once and is thread-safe.
 * The `getDatabase()` function returns the singleton instance of the database.
 *
 * @constructor Initializes the database and provides access to the MovieDao.
 */
@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

