package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.delivce.kikapu.R
import com.delivce.kikapu.ui.theme.AppColors

/**
 * Full-screen success celebration — plays the check + confetti once, then calls
 * [onFinished]. If the composition fails to load for any reason, it calls
 * [onFinished] immediately rather than blocking the flow.
 */
@Composable
fun SuccessOverlay(onFinished: () -> Unit) {
    val checkComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_success_check))
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_confetti_burst))
    val checkProgress by animateLottieCompositionAsState(composition = checkComposition, iterations = 1)
    val confettiProgress by animateLottieCompositionAsState(composition = confettiComposition, iterations = 1)

    LaunchedEffect(checkComposition) {
        if (checkComposition == null) onFinished()
    }
    LaunchedEffect(checkProgress) {
        if (checkComposition != null && checkProgress >= 1f) onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        if (confettiComposition != null) {
            LottieAnimation(
                composition = confettiComposition,
                progress = { confettiProgress },
                modifier = Modifier.size(260.dp)
            )
        }
        if (checkComposition != null) {
            LottieAnimation(
                composition = checkComposition,
                progress = { checkProgress },
                modifier = Modifier.size(140.dp)
            )
        }
    }
}
