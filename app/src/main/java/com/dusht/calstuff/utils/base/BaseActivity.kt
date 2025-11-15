package com.dusht.calstuff.utils.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base Activity for MVI Architecture
 * Handles common activity operations and manages ViewModel state/events
 */
abstract class BaseActivity<STATE : ViewState, EVENT : ViewEvent, EFFECT : ViewEffect> : ComponentActivity() {

    protected abstract val viewModel: BaseViewModel<STATE, EVENT, EFFECT>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupObservers()

        setContent {
            val state by viewModel.state.collectAsState()

            // Observe side effects
            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    handleEffect(effect)
                }
            }

            Content(state = state, onEvent = viewModel::handleEvent)
        }
    }

    /**
     * Composable content - implement in child activities
     */
    @Composable
    protected abstract fun Content(state: STATE, onEvent: (EVENT) -> Unit)

    /**
     * Handle one-time side effects (navigation, toasts, etc.)
     */
    protected open fun handleEffect(effect: EFFECT) {
        // Override in child activities to handle effects
    }

    /**
     * Setup lifecycle-aware observers
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeState()
            }
        }
    }

    /**
     * Override to observe specific state changes
     */
    protected open suspend fun observeState() {
        // Optional: Add state observation logic
    }

    /**
     * Send event to ViewModel
     */
    protected fun sendEvent(event: EVENT) {
        viewModel.handleEvent(event)
    }
}