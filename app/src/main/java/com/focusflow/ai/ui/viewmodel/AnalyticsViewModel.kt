package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.analytics.ProductivitySummary
import com.focusflow.ai.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val summary: ProductivitySummary? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = analyticsRepository.observeSummary()
        .map { AnalyticsUiState(isLoading = false, summary = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}
