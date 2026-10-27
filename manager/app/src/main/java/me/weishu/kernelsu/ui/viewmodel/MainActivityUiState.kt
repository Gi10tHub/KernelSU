package me.weishu.k10ernelsu.ui.viewmodel

import androidx.compose.runtime.Immutable
import me.weishu.k10ernelsu.ui.UiMode
import me.weishu.k10ernelsu.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val enableNavigationBadge: Boolean,
    val enableSwipeDismiss: Boolean,
    val pagerInterceptionMode: Int,
    val moduleDescriptionMaxLines: Int = 4,
    val uiMode: UiMode,
)
