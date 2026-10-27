package me.weishu.k10ernelsu.ui.component

import androidx.compose.runtime.Composable
import me.weishu.k10ernelsu.Natives

@Composable
fun KsuIsValid(
    content: @Composable () -> Unit
) {
    val isManager = Natives.isManager
    val ksuVersion = if (isManager) Natives.version else null

    if (ksuVersion != null) {
        content()
    }
}
