package com.dusht.calstuff.ui.screens.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.vm.PostLoginDestination
import com.dusht.calstuff.vm.PostLoginViewModel

@Composable
fun PostLoginScreen(
    onNavigateToProfileOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostLoginViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (val d = destination) {
            PostLoginDestination.ProfileOnboarding -> {
                viewModel.consumeDestination()
                onNavigateToProfileOnboarding()
            }
            PostLoginDestination.Home -> {
                viewModel.consumeDestination()
                onNavigateToHome()
            }
            null -> Unit
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
