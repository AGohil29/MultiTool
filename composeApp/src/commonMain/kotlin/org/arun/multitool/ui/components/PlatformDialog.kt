package org.arun.multitool.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun CommonAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Cancel"
)