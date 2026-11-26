package com.sippy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sippy.app.di.AppContainer
import com.sippy.app.ui.AppViewModel
import com.sippy.app.ui.SippyNavigation

class MainActivity : ComponentActivity() {
    private val container: AppContainer by lazy { (application as SippyApp).container }
    private val viewModel: AppViewModel by viewModels { SippyVmFactory(container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SippyNavigation(viewModel) }
    }
}

class SippyVmFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(
            repository = container.repository,
            settings = container.settings,
            clock = container.clock,
            scheduler = container.scheduler,
            notifier = container.notifier
        ) as T
    }
}
