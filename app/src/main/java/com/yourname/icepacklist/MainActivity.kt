package com.yourname.icepacklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var apiKeyDataStore: ApiKeyDataStore

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
            IcepackTheme(darkTheme = darkTheme) {
                IcepackNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

