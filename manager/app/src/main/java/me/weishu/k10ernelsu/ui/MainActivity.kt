package me.weishu.k10ernelsu.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults.flingBehavior
import androidx.compose.foundation.pager.PagerDefaults.pageNestedScrollConnection
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.k10ernelsu.Natives
import me.weishu.k10ernelsu.ui.component.bottombar.BottomBar
import me.weishu.k10ernelsu.ui.component.bottombar.MainPagerState
import me.weishu.k10ernelsu.ui.component.bottombar.NavigationBadgeState
import me.weishu.k10ernelsu.ui.component.bottombar.SideRail
import me.weishu.k10ernelsu.ui.component.bottombar.rememberMainPagerState
import me.weishu.k10ernelsu.ui.component.bottombar.useNavigationRail
import me.weishu.k10ernelsu.ui.navigation3.IntentDispatcher
import me.weishu.k10ernelsu.ui.navigation3.LocalNavigator
import me.weishu.k10ernelsu.ui.navigation3.Navigator
import me.weishu.k10ernelsu.ui.navigation3.Route
import me.weishu.k10ernelsu.ui.navigation3.rememberNavigator
import me.weishu.k10ernelsu.ui.screen.about.AboutScreen
import me.weishu.k10ernelsu.ui.screen.appprofile.AppProfileScreen
import me.weishu.k10ernelsu.ui.screen.colorpalette.ColorPaletteScreen
import me.weishu.k10ernelsu.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import me.weishu.k10ernelsu.ui.screen.flash.FlashScreen
import me.weishu.k10ernelsu.ui.screen.home.HomePager
import me.weishu.k10ernelsu.ui.screen.install.InstallScreen
import me.weishu.k10ernelsu.ui.screen.module.ModulePager
import me.weishu.k10ernelsu.ui.screen.modulerepo.ModuleRepoDetailScreen
import me.weishu.k10ernelsu.ui.screen.modulerepo.ModuleRepoScreen
import me.weishu.k10ernelsu.ui.screen.settings.SettingPager
import me.weishu.k10ernelsu.ui.screen.sulog.SulogScreen
import me.weishu.k10ernelsu.ui.screen.superuser.SuperUserPager
import me.weishu.k10ernelsu.ui.screen.template.AppProfileTemplateScreen
import me.weishu.k10ernelsu.ui.screen.templateeditor.TemplateEditorScreen
import me.weishu.k10ernelsu.ui.theme.KernelSUTheme
import me.weishu.k10ernelsu.ui.theme.LocalColorMode
import me.weishu.k10ernelsu.ui.theme.LocalEnableBlur
import me.weishu.k10ernelsu.ui.theme.LocalEnableFloatingBottomBar
import me.weishu.k10ernelsu.ui.theme.LocalEnableFloatingBottomBarBlur
import me.weishu.k10ernelsu.ui.theme.LocalEnableNavigationBadge
import me.weishu.k10ernelsu.ui.theme.LocalModuleDescriptionMaxLines
import me.weishu.k10ernelsu.ui.util.getSuperuserCount
import me.weishu.k10ernelsu.ui.util.install
import me.weishu.k10ernelsu.ui.util.rememberBlurBackdrop
import me.weishu.k10ernelsu.ui.viewmodel.MainActivityViewModel
import me.weishu.k10ernelsu.ui.viewmodel.MainPagerConfig
import me.weishu.k10ernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.k10ernelsu.ui.viewmodel.SuperUserViewModel
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.nav.transition.navGraphicsTransition
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.pagerGestureOverride

class MainActivity : ComponentActivity() {

    private val intentChannel = Channel<Intent>(capacity = Channel.BUFFERED)
    private var contentReady = false
    private var splashStartedAt = 0L
    private val splashAnimationDurationMs = 500L


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashStartedAt = SystemClock.uptimeMillis()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            !contentReady || SystemClock.uptimeMillis() - splashStartedAt < splashAnimationDurationMs
        }

        val isManager = Natives.isManager
        if (isManager && Natives.kernelUAPIVersion == Natives.managerUAPIVersion) install()

        if (savedInstanceState == null) intent?.let { intentChannel.trySend(it) }

        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val uiMode = uiState.uiMode
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }

            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalEnableBlur provides uiState.enableBlur,
                LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
                LocalEnableNavigationBadge provides uiState.enableNavigationBadge,
                LocalModuleDescriptionMaxLines provides uiState.moduleDescriptionMaxLines,
                LocalUiMode provides uiMode,
            ) {
                KernelSUTheme(appSettings = appSettings, uiMode = uiMode) {
                    IntentDispatcher(intentChannel = intentChannel)
                    val swipeDismiss = if (!uiState.compactDashboardMode && uiState.enableSwipeDismiss) {
                        if (LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            NavSwipeDirection.RightToLeft
                        } else {
                            NavSwipeDirection.LeftToRight
                        }
                    } else {
                        NavSwipeDirection.None
                    }
                    val flashRoute = navigator.current() as? Route.Flash
                    var flashBackEnabled by remember(flashRoute) { mutableStateOf(false) }
                    val mainScreenEntry = @Composable {
                        MainScreen(
                            initialPage = selectedMainPage,
                            pagerInterceptionMode = uiState.pagerInterceptionMode,
                            compactDashboardMode = uiState.compactDashboardMode,
                            onPageChanged = viewModel::setSelectedMainPage,
                        )
                    }

                    val navDisplay = @Composable {
                        NavDisplay(
                            backStack = navigator.backStack,
                            transition = if (uiState.compactDashboardMode) {
                                CompactDashboardTransition
                            } else {
                                NavTransitions.MiuixDefault
                            },
                            effects = NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius()),
                            onBack = {
                                when (val top = navigator.current()) {
                                    is Route.TemplateEditor -> {
                                        if (!top.readOnly) {
                                            navigator.setResult("template_edit", true)
                                        } else {
                                            navigator.pop()
                                        }
                                    }

                                    else -> navigator.pop()
                                }
                            }) {
                            entry<Route.Main>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.About>(swipeDismiss = swipeDismiss) { AboutScreen() }
                            entry<Route.Sulog>(swipeDismiss = swipeDismiss) { SulogScreen() }
                            entry<Route.ColorPalette>(swipeDismiss = swipeDismiss) { ColorPaletteScreen() }
                            entry<Route.AppProfileTemplate>(swipeDismiss = swipeDismiss) { AppProfileTemplateScreen() }
                            entry<Route.TemplateEditor>(swipeDismiss = swipeDismiss) { key -> TemplateEditorScreen(key.template, key.readOnly) }
                            entry<Route.AppProfile>(swipeDismiss = swipeDismiss) { key -> AppProfileScreen(key.uid) }
                            entry<Route.ModuleRepo>(swipeDismiss = swipeDismiss) { ModuleRepoScreen() }
                            entry<Route.ModuleRepoDetail>(swipeDismiss = swipeDismiss) { key -> ModuleRepoDetailScreen(key.module) }
                            entry<Route.Install>(swipeDismiss = swipeDismiss) { InstallScreen() }
                            entry<Route.Flash>(swipeDismiss = if (flashBackEnabled) swipeDismiss else NavSwipeDirection.None) { key ->
                                FlashScreen(
                                    key.flashIt,
                                    onBackEnabledChange = { enabled ->
                                        if (navigator.current() == key) flashBackEnabled = enabled
                                    },
                                )
                            }
                            entry<Route.ExecuteModuleAction>(swipeDismiss = swipeDismiss) { key ->
                                ExecuteModuleActionScreen(
                                    key.moduleId,
                                    key.fromShortcut
                                )
                            }
                            entry<Route.Home>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.SuperUser>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.Module>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.Settings>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                        }
                    }

                    when (uiMode) {
                        UiMode.Material -> androidx.compose.material3.Scaffold(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) { navDisplay() }

                        UiMode.Miuix -> Scaffold { navDisplay() }
                    }
                    SideEffect { contentReady = true }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentChannel.trySend(intent)
    }
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

private val CompactDashboardTransition: NavTransition = navGraphicsTransition(
    motion = NavMotion(
        commit = NavSettleSpec.Tween(durationMillis = 120, easing = FastOutSlowInEasing),
        cancel = NavSettleSpec.Tween(durationMillis = 120, easing = FastOutSlowInEasing),
        programmatic = NavSettleSpec.Tween(durationMillis = 150, easing = FastOutSlowInEasing),
    ),
) { scope ->
    val depth = scope.relativeDepth
    val progress = (if (depth <= 0f) {
        1f + depth.coerceAtLeast(-1f)
    } else {
        1f - depth.coerceAtMost(1f)
    }).coerceIn(0f, 1f)
    alpha = progress
    scaleX = 0.96f + 0.04f * progress
    scaleY = 0.96f + 0.04f * progress
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    pagerInterceptionMode: Int = PagerInterceptionMode.CrossAxisInterceptor.ordinal,
    compactDashboardMode: Boolean = false,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val enableBlur = LocalEnableBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val useNavigationRail = !compactDashboardMode && useNavigationRail(enableFloatingBottomBar)
    val pagerState = rememberPagerState(
        initialPage = if (compactDashboardMode) 0 else initialPage,
        pageCount = { MainPagerConfig.PAGE_COUNT },
    )
    val mainPagerState = rememberMainPagerState(
        pagerState = pagerState,
        animatePageChanges = !useNavigationRail,
    )
    val isFullFeatured = Natives.isFullFeatured()
    val pagerMode = if (compactDashboardMode) {
        PagerInterceptionMode.Native
    } else {
        PagerInterceptionMode.entries.getOrElse(pagerInterceptionMode) {
            PagerInterceptionMode.Native
        }
    }
    val interceptPagerGestures = pagerMode == PagerInterceptionMode.CrossAxisInterceptor
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }
    val pagerScrollEnabled = !compactDashboardMode && userScrollEnabled

    val enableNavigationBadge = LocalEnableNavigationBadge.current
    val badgeEnabled = enableNavigationBadge && isFullFeatured
    val moduleViewModel = viewModel<ModuleViewModel>()
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()

    val superUserViewModel = viewModel<SuperUserViewModel>()
    val grantedUidCount by remember(superUserViewModel) {
        superUserViewModel.uiState
            .map { state -> state.groupedApps.count { it.anyAllowSu } }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(0)

    LaunchedEffect(moduleViewModel, superUserViewModel) {
        moduleViewModel.initializePreferences()
        superUserViewModel.initializePreferences()

        coroutineScope {
            launch {
                val state = superUserViewModel.uiState.value
                if (!state.hasLoaded && !state.isRefreshing) {
                    superUserViewModel.loadAppList().join()
                } else if (!state.hasLoaded) {
                    superUserViewModel.uiState.first { it.hasLoaded }
                }
            }
            launch {
                val state = moduleViewModel.uiState.value
                if (!state.hasLoaded && !state.isRefreshing) {
                    moduleViewModel.fetchModuleList()
                }
                val loadedState = if (state.hasLoaded) {
                    state
                } else {
                    moduleViewModel.uiState.first { it.hasLoaded }
                }
                moduleViewModel.syncModuleUpdateInfo(loadedState.modules)
            }
        }
    }

    // Loading the app list just for a badge is too expensive; read the kernel allowlist instead.
    var superuserCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(badgeEnabled, grantedUidCount) {
        superuserCount = if (badgeEnabled) withContext(Dispatchers.IO) { getSuperuserCount() } else 0
    }

    val navigationBadge = if (badgeEnabled) {
        NavigationBadgeState(
            superuserCount = superuserCount,
            moduleEnabledCount = moduleUiState.modules.count { it.enabled },
            moduleUpdatableCount = moduleUiState.updateInfo.count { it.value.downloadUrl.isNotBlank() },
        )
    } else {
        NavigationBadgeState()
    }
    val uiMode = LocalUiMode.current
    val surfaceColor = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surface // Blur is not used in Material, this is just a placeholder
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    val blurBackdrop = rememberBlurBackdrop(enableBlur)

    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    LaunchedEffect(compactDashboardMode) {
        if (compactDashboardMode) {
            mainPagerState.pagerState.scrollToPage(0)
            mainPagerState.syncPage()
        }
    }

    BackHandler(
        enabled = compactDashboardMode &&
            navController.current() is Route.Main &&
            navController.backStackSize() == 1 &&
            currentPage != 0,
    ) {
        mainPagerState.scrollToPage(0)
    }
    MainScreenBackHandler(mainPagerState, navController, compactDashboardMode)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box(modifier = if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier) {
                HorizontalPager(
                    modifier = Modifier
                        .pagerGestureOverride(
                            pagerState = mainPagerState.pagerState,
                            mode = pagerMode,
                            enabled = pagerScrollEnabled,
                        )
                        .then(
                            if (!compactDashboardMode && enableFloatingBottomBar && enableFloatingBottomBarBlur) {
                                Modifier.layerBackdrop(backdrop)
                            } else {
                                Modifier
                            }
                        ),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = 3,
                    overscrollEffect = null,
                    userScrollEnabled = pagerScrollEnabled && !interceptPagerGestures,
                    pageNestedScrollConnection = if (interceptPagerGestures) {
                        PagerGestureNestedScrollConnection
                    } else {
                        pageNestedScrollConnection(
                            state = mainPagerState.pagerState,
                            orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                        )
                    },
                    flingBehavior = flingBehavior(
                        state = mainPagerState.pagerState,
                        snapAnimationSpec = PagerNavigationSpringSpec,
                    ),
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> {
                            HomePager(
                                navigator = navController,
                                bottomInnerPadding = bottomInnerPadding,
                                isCurrentPage = isCurrentPage,
                                compactDashboardMode = compactDashboardMode,
                            )
                        }
                        1 -> SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                        2 -> ModulePager(bottomInnerPadding, isCurrentPage)
                        3 -> SettingPager(navController, bottomInnerPadding, isCurrentPage)
                    }
                }
            }
        }

        if (compactDashboardMode) {
            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }

                UiMode.Miuix -> Scaffold { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }
            }
        } else if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row {
                        SideRail(navigationBadge)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }

                UiMode.Miuix -> Scaffold { _ ->
                    Row {
                        SideRail(navigationBadge)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }
            }
        } else {
            val bottomBar = @Composable {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomBar(
                        blurBackdrop = blurBackdrop,
                        backdrop = backdrop,
                        navigationBadge = navigationBadge,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(
                    bottomBar = bottomBar,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }

                UiMode.Miuix -> Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }
            }
        }
    }
}


@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
    compactDashboardMode: Boolean,
) {
    val isPagerBackHandlerEnabled by remember(compactDashboardMode) {
        derivedStateOf {
            !compactDashboardMode &&
                navController.current() is Route.Main &&
                navController.backStackSize() == 1 &&
                mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}
