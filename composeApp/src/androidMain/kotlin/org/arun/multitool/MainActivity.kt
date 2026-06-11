package org.arun.multitool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.arun.multitool.utils.AndroidPermissionManager
import org.arun.multitool.utils.PermissionManager
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Update Koin with the current activity context
        loadKoinModules(module {
            single<PermissionManager> { AndroidPermissionManager(this@MainActivity) }
        })
        setContent {
            App(isIOS = false)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(isIOS = false)
}