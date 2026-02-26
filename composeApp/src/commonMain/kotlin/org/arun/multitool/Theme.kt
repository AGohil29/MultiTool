package org.arun.multitool

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import multitool.composeapp.generated.resources.Res
import multitool.composeapp.generated.resources.roboto_mono
import org.jetbrains.compose.resources.Font

@Composable
fun getRobotoMonoFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.roboto_mono)
    )
}