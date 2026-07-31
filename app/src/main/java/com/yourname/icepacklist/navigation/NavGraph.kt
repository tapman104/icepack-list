package com.yourname.icepacklist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.icepacklist.feature.detail.ui.DetailScreen
import com.yourname.icepacklist.feature.home.ui.HomeScreen
import com.yourname.icepacklist.feature.home.ui.CategoryListScreen
import com.yourname.icepacklist.feature.search.ui.SearchScreen
import com.yourname.icepacklist.feature.settings.SettingsScreen
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    
    val startDestination = if (apiKey.isNullOrBlank()) Routes.Settings.route else Routes.Home.route

    IcepackScaffold(
        navController = navController,
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
                    onViewCategory = { category ->
                        navController.navigate(Routes.CategoryList.createRoute(category))
                    }
                )
            }

            composable(Routes.Search.route) {
                SearchScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Routes.Detail.buildRoute(movieId))
                    }
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
                DetailScreen()
            }

            composable(
                route = Routes.CategoryList.route,
                arguments = listOf(
                    navArgument(Routes.CategoryList.CATEGORY_ARG) { type = NavType.StringType }
                )
            ) {
                CategoryListScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
