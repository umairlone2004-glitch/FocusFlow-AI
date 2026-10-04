package com.focusflow.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.navigation.FocusFlowNavHost
import com.focusflow.ai.ui.screens.OnboardingScreen
import com.focusflow.ai.ui.theme.FocusFlowTheme
import com.focusflow.ai.ui.viewmodel.AppViewModel
import com.focusflow.ai.ui.viewmodel.RootUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
            val profile by appViewModel.profile.collectAsStateWithLifecycle()

            FocusFlowTheme(themeMode = themeMode) {
                NotificationPermissionEffect()

                val current = profile
                val rootState = when {
                    current == null -> RootUiState.Loading
                    !current.onboardingComplete -> RootUiState.Onboarding
                    else -> RootUiState.Ready
                }

                when (rootState) {
                    RootUiState.Loading -> LoadingScreen()
                    RootUiState.Onboarding -> OnboardingScreen(
                        onComplete = { name, goal, theme ->
                            appViewModel.completeOnboarding(name, goal, theme)
                        }
                    )
                    RootUiState.Ready -> FocusFlowNavHost()
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun NotificationPermissionEffect() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
