package me.weishu.k10ernelsu.ui.component.choosekmidialog

import androidx.compose.runtime.Composable
import me.weishu.k10ernelsu.ui.LocalUiMode
import me.weishu.k10ernelsu.ui.UiMode

@Composable
fun ChooseKmiDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> ChooseKmiDialogMiuix(show, onDismissRequest, onSelected)
        UiMode.Material -> ChooseKmiDialogMaterial(show, onDismissRequest, onSelected)
    }
}
