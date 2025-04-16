package com.example.sagenote.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

/**
 * Animation utilities for SageNote app transitions
 */
object AnimationUtils {
    private const val ANIMATION_DURATION = 350

    /**
     * Creates an expand-in enter transition (Google Keep style)
     */
    fun enterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        expandIn(
            expandFrom = androidx.compose.ui.Alignment.Center,
            initialSize = { fullSize -> IntSize(fullSize.width / 2, fullSize.height / 3) },
            animationSpec = tween(ANIMATION_DURATION, easing = EaseInOutCubic)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION / 2, delayMillis = ANIMATION_DURATION / 3))
    }

    /**
     * Creates a fade-out exit transition
     */
    fun exitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(ANIMATION_DURATION / 2))
    }

    /**
     * Creates a fade-in pop enter transition
     */
    fun popEnterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(ANIMATION_DURATION / 2))
    }

    /**
     * Creates a shrink-out pop exit transition (Google Keep style)
     */
    fun popExitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        shrinkOut(
            shrinkTowards = androidx.compose.ui.Alignment.Center,
            targetSize = { fullSize -> IntSize(fullSize.width / 2, fullSize.height / 3) },
            animationSpec = tween(ANIMATION_DURATION, easing = EaseInOutCubic)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION / 2))
    }
}
