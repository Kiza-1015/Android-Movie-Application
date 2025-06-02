# 🎬 Movie Title Search App (Android, Kotlin, Jetpack Compose)

This is a simple Android application built with Kotlin and Jetpack Compose that allows users to search for movies by entering partial titles. It fetches data from the [OMDb API](https://www.omdbapi.com/) and displays the results in a user-friendly UI.

---

## 🚀 Features

- 🔍 Search for movies using partial titles
- 📋 Display detailed movie information (rating, release date, runtime, genre, director, cast)
- 📡 Asynchronous API calls using Kotlin Coroutines
- 🎨 UI built with Jetpack Compose and Material3
- ⏳ Loading indicator and error handling
- 🔙 Navigation back to the main screen

---

## 📦 Project Structure

- **`MovieSearchResult.kt`**  
  Data model to hold movie details from the API response.

- **`SearchAll` Activity**  
  Hosts the `AllSearch` composable which manages state, handles UI events, and displays the list of results.

- **`AllSearch()` Composable**  
  - Text field input for movie titles  
  - Button to initiate search  
  - Circular loading indicator  
  - LazyColumn to show matching movies in Cards

- **`searchMoviesEnhanced()`**  
  Performs multi-phase search with fallback strategies (wildcard, prepending common words).

- **`searchMoviesByPartialTitle()`**  
  Hits OMDb's `s=` search endpoint, then uses `i=` endpoint to retrieve full details for each result.

---

## 🧪 Requirements

- Android Studio Electric Eel or later
- Kotlin 1.8+
- Internet connection (app uses OMDb API)
- Jetpack Compose setup

---

## 🔑 API Key

This project uses the OMDb API. You must obtain a free API key from [http://www.omdbapi.com/apikey.aspx](http://www.omdbapi.com/apikey.aspx).

Replace the placeholder key in the code:

```kotlin
val apiKey = "e4b4398" // Replace with your own key
