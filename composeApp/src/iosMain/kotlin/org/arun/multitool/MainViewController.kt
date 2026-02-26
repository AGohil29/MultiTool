package org.arun.multitool

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import org.arun.multitool.di.initKoin
import platform.UIKit.UIColor
import platform.UIKit.UIView

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(someText: String) = ComposeUIViewController {
    initKoin()
    App(isIOS = true, someText = someText)
}