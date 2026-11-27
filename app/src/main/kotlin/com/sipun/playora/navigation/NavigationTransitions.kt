package com.sipun.playora.navigation

import android.view.RoundedCorner
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.sipun.playora.core.ui.components.thenIf

private const val PUSH_DURATION_MILLIS = 320
private const val POP_DURATION_MILLIS = 360
private val PushEasing = CubicBezierEasing(
    0.3f,
    0.7f,
    0.5f,
    1f,
)
private val PopEasing = CubicBezierEasing(
    0.3f,
    0.3f,
    0.3f,
    1f,
)
private const val PARALLAX_FRACTION = 0.3f
private const val PREDICTIVE_PARALLAX_FRACTION = 0.25f
private const val MAX_SCRIM_ALPHA = 0.5f

private fun Scene<NavKey>.isPoppingTo(targetState: Scene<NavKey>): Boolean = previousEntries.any {
    it.contentKey == targetState.entries.lastOrNull()?.contentKey
}

internal fun navigationTransition(
    initialState: Scene<NavKey>,
    targetState: Scene<NavKey>,
    isPredictive: Boolean = false,
): ContentTransform {
    val isPop = initialState.isPoppingTo(targetState)
    val animationSpec = when {
        isPredictive -> tween<IntOffset>(
            durationMillis = POP_DURATION_MILLIS,
            easing = LinearEasing,
        )

        isPop -> tween(
            durationMillis = POP_DURATION_MILLIS,
            easing = PopEasing,
        )

        else -> tween(
            durationMillis = PUSH_DURATION_MILLIS,
            easing = PushEasing,
        )
    }
    val parallax = if (isPredictive) PREDICTIVE_PARALLAX_FRACTION else PARALLAX_FRACTION
    return slideInHorizontally(
        initialOffsetX = { if (isPop) -(it * parallax).toInt() else it },
        animationSpec = animationSpec,
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { if (isPop) it else -(it * parallax).toInt() },
        animationSpec = animationSpec,
    )
}

@Composable
internal fun navigationTransitionModifier(entry: NavEntry<NavKey>): Modifier {
    val visibility = LocalNavAnimatedContentScope.current.transition

    @Suppress("UNCHECKED_CAST")
    val scenes = visibility.parentTransition as? Transition<Scene<NavKey>> ?: return Modifier
    val initialState = scenes.currentState
    val targetState = scenes.targetState
    val isPop = initialState.isPoppingTo(targetState)
    val scrimAlpha = visibility.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = if (isPop) POP_DURATION_MILLIS else PUSH_DURATION_MILLIS,
                easing = LinearEasing,
            )
        },
        label = "navigation_scrim",
    ) { if (it == EnterExitState.Visible) 0f else MAX_SCRIM_ALPHA }
    val cornerShape = rememberDisplayCornerShape()

    if (initialState == targetState) {
        return Modifier
    }
    val bottomScene = if (isPop) targetState else initialState
    return if (entry.contentKey == bottomScene.entries.lastOrNull()?.contentKey) {
        Modifier.drawWithContent {
            drawContent()
            drawRect(
                color = Color.Black,
                alpha = scrimAlpha.value,
            )
        }
    } else {
        Modifier.thenIf(cornerShape != null) { clip(cornerShape!!) }
    }
}

@Composable
private fun rememberDisplayCornerShape(): Shape? {
    val insets = LocalView.current.rootWindowInsets ?: return null
    return remember(insets) {
        fun radius(position: Int) = insets.getRoundedCorner(position)?.radius?.toFloat() ?: 0f
        val topLeft = radius(RoundedCorner.POSITION_TOP_LEFT)
        val topRight = radius(RoundedCorner.POSITION_TOP_RIGHT)
        val bottomRight = radius(RoundedCorner.POSITION_BOTTOM_RIGHT)
        val bottomLeft = radius(RoundedCorner.POSITION_BOTTOM_LEFT)
        if (topLeft + topRight + bottomRight + bottomLeft == 0f) {
            null
        } else {
            AbsoluteRoundedCornerShape(
                topLeft = topLeft,
                topRight = topRight,
                bottomRight = bottomRight,
                bottomLeft = bottomLeft,
            )
        }
    }
}
