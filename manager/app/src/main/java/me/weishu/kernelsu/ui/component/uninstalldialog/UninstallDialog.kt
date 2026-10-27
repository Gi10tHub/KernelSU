package me.weishu.k10ernelsu.ui.component.uninstalldialog

import androidx.compose.runtime.Composable
import me.weishu.k10ernelsu.ui.LocalUiMode
import me.weishu.k10ernelsu.ui.UiMode

@Composable
fun UninstallDialog(
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> UninstallDialogMiuix(show, onDismissRequest)
        UiMode.Material -> UninstallDialogMaterial(show, onDismissRequest)
    }
}
