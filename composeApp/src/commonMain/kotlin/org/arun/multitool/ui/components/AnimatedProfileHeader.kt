package org.arun.multitool.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import org.arun.multitool.ui.transition.TransitionHandler
import org.koin.compose.koinInject

@Composable
fun AnimatedProfileHeader(name: String, isExpanded: Boolean, onClick: () -> Unit) {
    val transition = updateTransition(targetState = isExpanded, label = "ProfileToggle")
    val transitionHandler = koinInject<TransitionHandler>()
    val sharedData by transitionHandler.state // Observe the shared state

    // Use sharedData.initialBounds to calculate your animation
    // Animate a 'progress' value from 0 to 1
    val progress by animateFloatAsState(
        targetValue = if (sharedData.isTransitioning) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SharedElementProgress"
    )

    // If progress is 0, we are at list item position. If 1, we are at top.
    val offsetY = lerp(sharedData.initialBounds.top, 0f, progress)
    val scale = lerp(0.7f, 1f, progress)

    // 2. Define animated properties
    val size by transition.animateDp(label = "Size") { expanded ->
        if (expanded) 200.dp else 100.dp
    }

    val cornerRadius by transition.animateInt(label = "Corners") { expanded ->
        if (expanded) 24 else 50 // Transitions from Circle to Rounded Rect
    }

    LaunchedEffect(Unit) {
        // Start the animation to the final position
        transitionHandler.completeTransition()
    }

    Surface(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                translationY = offsetY
                scaleX = scale
                scaleY = scale
                // Fade in the rest of the screen as it flies up
                alpha = progress
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}