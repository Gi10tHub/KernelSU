package me.weishu.k10ernelsu.ui.screen.templateeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.k10ernelsu.R
import me.weishu.k10ernelsu.ui.LocalCompactDashboardMode
import me.weishu.k10ernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.k10ernelsu.ui.component.material.SegmentedColumn
import me.weishu.k10ernelsu.ui.component.material.SegmentedTextField
import me.weishu.k10ernelsu.ui.component.material.TopBarBackButton
import me.weishu.k10ernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.k10ernelsu.ui.component.profile.RootProfileConfig

@Composable
fun TemplateEditorScreenMaterial(
    state: TemplateEditorUiState,
    actions: TemplateEditorActions,
) {
    val compactDashboardMode = LocalCompactDashboardMode.current
    val scrollBehavior = if (compactDashboardMode) null else
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ExpressiveScaffold(
        topBar = {
            TopBar(
                title = if (state.isCreation) {
                    stringResource(R.string.app_profile_template_create)
                } else if (state.readOnly) {
                    stringResource(R.string.app_profile_template_view)
                } else {
                    stringResource(R.string.app_profile_template_edit)
                },
                readOnly = state.readOnly,
                summary = state.titleSummary,
                onBack = actions.onBack,
                onDelete = actions.onDelete,
                onSave = actions.onSave,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 13.dp)
                .imePadding()
                .then(
                    if (scrollBehavior == null) Modifier
                    else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                )
                .verticalScroll(rememberScrollState())
        ) {
            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                content = buildList {
                    add(
                        {
                            TemplateEditorListItem(
                                label = stringResource(id = R.string.app_profile_template_name),
                                value = state.template.name,
                                readOnly = state.readOnly,
                                onValueChange = actions.onNameChange,
                            )
                        }
                    )
                    if (state.isCreation) {
                        add(
                            {
                                TemplateEditorListItem(
                                    label = stringResource(id = R.string.app_profile_template_id),
                                    value = state.template.id,
                                    errorHint = state.idErrorHint,
                                    isError = state.idErrorHint.isNotEmpty(),
                                    readOnly = state.readOnly,
                                    onValueChange = actions.onIdChange,
                                )
                            }
                        )
                    }
                    add(
                        {
                            TemplateEditorListItem(
                                label = stringResource(id = R.string.module_author),
                                value = state.template.author,
                                readOnly = state.readOnly,
                                onValueChange = actions.onAuthorChange,
                            )
                        }
                    )
                    add(
                        {
                            TemplateEditorListItem(
                                label = stringResource(id = R.string.app_profile_template_description),
                                value = state.template.description,
                                multiline = true,
                                readOnly = state.readOnly,
                                onValueChange = actions.onDescriptionChange,
                            )
                        }
                    )
                }
            )

            RootProfileConfig(
                fixedName = true,
                enabled = !state.readOnly,
                profile = toNativeProfile(state.template),
                onProfileChange = actions.onProfileChange,
            )

            Spacer(
                Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            )
        }
    }
}

@Composable
private fun TemplateEditorListItem(
    label: String,
    value: String,
    errorHint: String = "",
    isError: Boolean = false,
    multiline: Boolean = false,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    SegmentedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        supportingContent = if (isError && errorHint.isNotEmpty()) {
            { Text(errorHint, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
        } else null,
        isError = isError,
        singleLine = !multiline,
        minLines = 1,
        maxLines = if (multiline) 100 else 1,
        readOnly = readOnly
    )
}

@Composable
private fun TopBar(
    title: String,
    readOnly: Boolean,
    summary: String = "",
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val titleContent: @Composable () -> Unit = {
            Column {
                Text(title)
                if (summary.isNotBlank()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    val navigationIcon: @Composable () -> Unit = { TopBarBackButton(onClick = onBack) }
    val actions: @Composable RowScope.() -> Unit = {
        if (!readOnly) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = stringResource(id = R.string.app_profile_template_delete)
                )
            }
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.app_profile_template_save)
                )
            }
        }
    }
    val colors = expressiveTopAppBarColors()
    val windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    if (LocalCompactDashboardMode.current) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = windowInsets,
        )
    } else {
        LargeFlexibleTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior
        )
    }
}
