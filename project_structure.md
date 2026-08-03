# Project Structure and Responsibilities

This document outlines the architecture and file responsibilities for the Icepack project. It follows a feature-based modular structure using modern Android development practices (Jetpack Compose, Room, Retrofit, Coroutines/Flow).

## Core Architecture (`app/src/main/java/com/.../core/`)

These packages contain the foundational layers of the application that are shared across different features.

### `database/`
Handles local data persistence using Room.
*   **`IcepackDatabase.kt`**: The main Room database configuration.
*   **`dao/`**: Data Access Objects (DAOs) for interacting with the database tables (e.g., `MovieDao.kt`, `WatchlistDao.kt`).
*   **`entity/`**: Room entity classes representing database tables (e.g., `MovieEntity.kt`, `WatchlistEntity.kt`).

### `network/`
Manages all external API communication.
*   **`TmdbApiService.kt`**: Retrofit interface defining the API endpoints for TMDB.
*   **Interceptors**: Network interceptors for handling authentication (`AuthInterceptor.kt`), rate limiting (`RateLimitInterceptor.kt`), and automatic retries (`RetryInterceptor.kt`).

### `di/`
Dependency Injection configuration (likely using Hilt or Dagger).
*   **`DatabaseModule.kt`**: Provides Room database and DAO instances.
*   **`NetworkModule.kt`**: Provides Retrofit, OkHttp, and API service instances.

### `datastore/`
Handles local preferences and settings using Jetpack DataStore.
*   **`ApiKeyDataStore.kt`**: Manages the storage and retrieval of the user's API keys securely.

### `navigation/`
Manages routing and navigation between screens.
*   **`NavGraph.kt` & `Routes.kt`**: Defines the application's navigation graph and screen destinations.
*   **`IcepackScaffold.kt`**: The main app layout container holding the navigation components.

### `ui/`
Shared UI components and theme definitions used across multiple features.
*   **`theme/`**: Contains app styling, colors, and typography (`IcepackTheme.kt`).
*   **Components**: Reusable Compose UI elements like `MovieCard.kt`, `TvShowCard.kt`, `CategoryRow.kt`, and shimmer loading effects (`ShimmerBox.kt`, `ShimmerGrid.kt`).

---

## Features (`app/src/main/java/com/.../feature/`)

The application is divided into logical feature modules, each containing its own UI, ViewModel, and Data/Domain layers.

### `home/`
Handles the main landing screens and data models.
*   **`domain/`**: Contains core data models used throughout the app (`Movie.kt`, `TvShow.kt`, `Person.kt`, etc.).
*   **`data/`**: Repositories and paging sources for fetching home screen data (`HomeRepository.kt`, `CategoryPagingSource.kt`).
*   **`ui/`**: Screens and ViewModels for the home feed and category lists (`HomeScreen.kt`, `HomeViewModel.kt`).

### `detail/`
Manages the detailed view of movies and TV shows.
*   **`data/`**: `DetailRepository.kt` fetches detailed information for media.
*   **`ui/`**: Composable screens and sections for showing media details, cast (`DetailCastSection.kt`), episodes (`SeasonEpisodesScreen.kt`), recommendations, and the ViewModels managing their state.

### `search/`
Handles search functionality.
*   **`data/`**: `SearchRepository.kt` and `SearchPagingSource.kt` handle querying the API for search results.
*   **`ui/`**: `SearchScreen.kt` and `SearchViewModel.kt` manage the search UI and input state.

### `person/`
Displays details about actors and crew members.
*   **`ui/`**: `PersonDetailScreen.kt` and its ViewModel/Shimmer components.

### `watchlist/`
Manages the user's saved movies and TV shows.
*   **`data/`**: `WatchlistRepository.kt` handles adding/removing items from the local Room database and syncing.
*   **`ui/`**: `WatchlistScreen.kt` displays the user's saved lists.

### `settings/`
Manages app preferences.
*   **`ui/`**: `SettingsScreen.kt` and `SettingsViewModel.kt` for configuring the app.

---

## Resources (`app/src/main/res/`)

Contains static resources used by the application.
*   **`drawable/` & `mipmap/`**: App icons and placeholder images.
*   **`values/`**: `colors.xml`, `strings.xml`, and `themes.xml` for core Android resources.
