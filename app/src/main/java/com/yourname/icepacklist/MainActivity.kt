package com.yourname.icepacklist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.yourname.icepacklist.navigation.IcepackNavGraph
import com.yourname.icepacklist.ui.theme.IcepackTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import javax.inject.Inject
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import com.yourname.icepacklist.core.datastore.ThemeMode
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import com.yourname.icepacklist.feature.settings.SettingsViewModel
import com.yourname.icepacklist.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var apiKeyDataStore: ApiKeyDataStore
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val navigationFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by apiKeyDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            val navController = rememberNavController()
            
            LaunchedEffect(Unit) {
                navigationFlow.collect { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            }
            
            IcepackTheme(darkTheme = darkTheme) {
                IcepackNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController
                )
            }
        }
        
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                settingsViewModel.importBackup(uri, contentResolver)
                navigationFlow.tryEmit(Routes.Settings.route)
            }
        }
    }
}
