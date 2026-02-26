import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.arun.multitool.App
import org.arun.multitool.DesktopNotifier
import org.arun.multitool.TimerViewModel
import org.arun.multitool.di.initKoin


fun main() = application {
    initKoin()
    Window(onCloseRequest = ::exitApplication, title = "MultiTool") {
        // Use your shared App composable!
        App(false, someText = "Hello from Desktop")
    }
}