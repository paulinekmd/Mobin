package com.mobin.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.util.DataStoreManager
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            DataStoreManager.setOnboardingComplete()
            onDone()
        }
    }
}
