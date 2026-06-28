package com.android.setupwizard.navigation

import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.graphics.PathParser
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.setupwizard.ui.language.LanguageScreen
import com.android.setupwizard.ui.welcome.WelcomeScreen
import java.util.Locale
import kotlin.math.roundToInt

private object SetupWizardRoute {
    const val Welcome = "welcome"
    const val Language = "language"
}

private object SetupWizardMotionSpec {
    private val linearInterpolator = LinearInterpolator()
    private val fastOutSlowInInterpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    private val fastOutExtraSlowInInterpolator = PathInterpolator(
        PathParser.createPathFromPathData(
            "M 0,0 C 0.05,0 0.133333,0.06 0.166666,0.4 C 0.208333,0.82 0.25,1 1,1",
        ),
    )

    val LinearAlpha = Easing { fraction ->
        linearInterpolator.getInterpolation(fraction)
    }

    val FastOutSlowIn = Easing { fraction ->
        fastOutSlowInInterpolator.getInterpolation(fraction)
    }

    val FastOutExtraSlowIn = Easing { fraction ->
        fastOutExtraSlowInInterpolator.getInterpolation(fraction)
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.openEnter(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(durationMillis = 450, easing = SetupWizardMotionSpec.FastOutExtraSlowIn),
        initialOffsetX = { fullWidth -> (fullWidth * 0.10f).roundToInt() },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 83,
            delayMillis = 50,
            easing = SetupWizardMotionSpec.LinearAlpha,
        ),
        initialAlpha = 0f,
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.openExit(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(durationMillis = 450, easing = SetupWizardMotionSpec.FastOutSlowIn),
        targetOffsetX = { fullWidth -> -(fullWidth * 0.05f).roundToInt() },
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.closeEnter(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(durationMillis = 450, easing = SetupWizardMotionSpec.FastOutExtraSlowIn),
        initialOffsetX = { fullWidth -> -(fullWidth * 0.10f).roundToInt() },
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.closeExit(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(durationMillis = 450, easing = SetupWizardMotionSpec.FastOutExtraSlowIn),
        targetOffsetX = { fullWidth -> (fullWidth * 0.10f).roundToInt() },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 83,
            delayMillis = 35,
            easing = SetupWizardMotionSpec.LinearAlpha,
        ),
        targetAlpha = 0f,
    )
}

@Composable
fun SetupWizardHost(
    selectedLocale: Locale,
    onLocaleSelected: (Locale) -> Unit,
    onSetupFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupWizardRoute.Welcome,
        modifier = modifier,
        enterTransition = { openEnter() },
        exitTransition = { openExit() },
        popEnterTransition = { closeEnter() },
        popExitTransition = { closeExit() },
    ) {
        composable(route = SetupWizardRoute.Welcome) {
            WelcomeScreen(
                onNext = { navController.navigate(SetupWizardRoute.Language) },
            )
        }
        composable(route = SetupWizardRoute.Language) {
            LanguageScreen(
                selectedLocale = selectedLocale,
                onLocaleSelected = onLocaleSelected,
                onNext = onSetupFinished,
            )
        }
    }
}
