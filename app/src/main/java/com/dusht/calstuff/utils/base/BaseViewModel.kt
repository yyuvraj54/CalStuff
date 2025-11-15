package com.dusht.calstuff.utils.base


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI Architecture
 *
 * @param STATE: Immutable state representing the UI
 * @param EVENT: User actions/events from the UI
 * @param EFFECT: One-time side effects (navigation, toast, etc.)
 */
abstract class BaseViewModel<STATE : ViewState, EVENT : ViewEvent, EFFECT : ViewEffect>(
    initialState: STATE
) : ViewModel() {

    // State (UI state)
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    // Effect (One-time events)
    private val _effect = Channel<EFFECT>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * Current state value
     */
    protected val currentState: STATE
        get() = _state.value

    /**
     * Handle incoming events from UI
     */
    abstract fun handleEvent(event: EVENT)

    /**
     * Update the state
     */
    protected fun setState(reducer: STATE.() -> STATE) {
        _state.update(reducer)
    }

    /**
     * Send a one-time effect
     */
    protected fun sendEffect(effect: EFFECT) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Execute async operations safely
     */
    protected fun launchIO(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Override to handle errors globally
     */
    protected open fun handleError(exception: Exception) {
        // Log error or send error effect
        exception.printStackTrace()
    }
}


/**
 * Marker interface for View States
 */
interface ViewState

/**
 * Marker interface for View Events (user actions)
 */
interface ViewEvent

/**
 * Marker interface for View Effects (one-time side effects)
 */
interface ViewEffect