package org.arun.multitool.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun CommonAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
) {
    // todo
}