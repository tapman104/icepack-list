package com.yourname.icepacklist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.icepacklist.feature.detail.ui.DetailScreen
import com.yourname.icepacklist.feature.detail.ui.TvDetailScreen
import com.yourname.icepacklist.feature.home.ui.HomeScreen
import com.yourname.icepacklist.feature.home.ui.CategoryListScreen
import com.yourname.icepacklist.feature.person.ui.PersonDetailScreen
import com.yourname.icepacklist.feature.search.ui.SearchScreen
import com.yourname.icepacklist.feature.settings.ApiKeyScreen
import com.yourname.icepacklist.feature.settings.SettingsScreen
import com.yourname.icepacklist.feature.watchlist.ui.WatchlistScreen
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

@HiltViewModel
class NavViewModel @Inject constructor(
    apiKeyDataStore: ApiKeyDataStore
) : ViewModel() {
    val apiKey = apiKeyDataStore.apiKey
}

@Composable
fun IcepackNavGraph(modifier: Modifier = Modifier, navViewModel: NavViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val apiKey by navViewModel.apiKey.collectAsState(initial = null)
    
    val startDestination = if (apiKey.isNullOrBlank()) Routes.ApiKey.route else Routes.Home.route
    var isScrollingUp by remember { mutableStateOf(true) }

    IcepackScaffold(
        navController = navController,
        isScrollingUp = isScrollingUp,
        modifier = modifier
    ) { innerModifier ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = innerModifier
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.Detail.buildRoute(movieId))
                    },
                    onTvShowClick = { tvId ->
                        navController.navigate(Routes.TvDetail.buildRoute(tvId))
                    },
                    onViewCategory = { category ->
                        navController.navigate(Routes.CategoryList.createRoute(category))
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.Settings.route)
                    },
                    onScrollUp = { scrollingUp ->
                        isScrollingUp = scrollingUp
                    }
                )
            }

            composable(Routes.Search.route) {
                SearchScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.Detail.buildRoute(movieId))
                    },
                    onTvShowClick = { tvId ->
                        navController.navigate(Routes.TvDetail.buildRoute(tvId))
                    },
                    onPersonClick = { personId ->
                        navController.navigate(Routes.PersonDetail.createRoute(personId))
                    }
                )
            }

            composable(Routes.Watchlist.route) {
                WatchlistScreen(
                    onMovieClick = { navController.navigate(Routes.Detail.buildRoute(it)) },
                    onTvShowClick = { navController.navigate(Routes.TvDetail.buildRoute(it)) }
                )
            }

            composable(Routes.ApiKey.route) {
                ApiKeyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Routes.Detail.route,
                arguments = listOf(
                    navArgument(Routes.Detail.ARG_MOVIE_ID) {
                        type = NavType.IntType
                    }
                )
            ) {
                DetailScreen(
                    onBack = { navController.popBackStack() },
                    onMovieClick = { navController.navigate(Routes.Detail.buildRoute(it)) },
                    onPersonClick = { navController.navigate(Routes.PersonDetail.createRoute(it)) },
                    onFullCastClick = { id, type -> navController.navigate(Routes.FullCast.buildRoute(id, type)) }
                )
            }

            composable(
                route = Routes.TvDetail.route,
                arguments = listOf(
                    navArgument(Routes.TvDetail.ARG_TV_ID) { type = NavType.IntType }
                )
            ) {
                TvDetailScreen(
                    onBack = { navController.popBackStack() },
                    onTvShowClick = { navController.navigate(Routes.TvDetail.buildRoute(it)) },
                    onPersonClick = { navController.navigate(Routes.PersonDetail.createRoute(it)) },
                    onSeasonClick = { id, name, totalSeasons -> navController.navigate(Routes.SeasonEpisodes.buildRoute(id, name, totalSeasons)) },
                    onFullCastClick = { id, type -> navController.navigate(Routes.FullCast.buildRoute(id, type)) }
                )
            }

            composable(
                route = Routes.CategoryList.route,
                arguments = listOf(
                    navArgument(Routes.CategoryList.CATEGORY_ARG) { type = NavType.StringType }
                )
            ) {
                CategoryListScreen(
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movieId -> navController.navigate(Routes.Detail.buildRoute(movieId)) },
                    onTvShowClick = { tvId -> navController.navigate(Routes.TvDetail.buildRoute(tvId)) }
                )
            }

            composable(
                route = Routes.PersonDetail.route,
                arguments = listOf(
                    navArgument(Routes.PersonDetail.ARG_PERSON_ID) { type = NavType.IntType }
                )
            ) {
                PersonDetailScreen(
                    onBack = { navController.popBackStack() },
                    onMovieClick = { navController.navigate(Routes.Detail.buildRoute(it)) },
                    onTvClick = { navController.navigate(Routes.TvDetail.buildRoute(it)) }
                )
            }

            composable(
                route = Routes.SeasonEpisodes.route,
                arguments = listOf(
                    navArgument(Routes.SeasonEpisodes.ARG_TV_ID) { type = NavType.IntType },
                    navArgument(Routes.SeasonEpisodes.ARG_TV_NAME) { type = NavType.StringType },
                    navArgument(Routes.SeasonEpisodes.ARG_TOTAL_SEASONS) { type = NavType.IntType }
                )
            ) {
                com.yourname.icepacklist.feature.detail.ui.SeasonEpisodesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.FullCast.route,
                arguments = listOf(
                    navArgument(Routes.FullCast.ARG_MEDIA_ID) { type = NavType.IntType },
                    navArgument(Routes.FullCast.ARG_MEDIA_TYPE) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getInt(Routes.FullCast.ARG_MEDIA_ID) ?: 0
                val mediaType = backStackEntry.arguments?.getString(Routes.FullCast.ARG_MEDIA_TYPE) ?: ""
                com.yourname.icepacklist.feature.detail.ui.FullCastScreen(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    onBack = { navController.popBackStack() },
                    onPersonClick = { navController.navigate(Routes.PersonDetail.createRoute(it)) }
                )
            }
        }
    }
}
