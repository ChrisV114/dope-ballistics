package za.co.dope.ballistics.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.SessionRepository
import za.co.dope.ballistics.ui.screens.AmmunitionScreen
import za.co.dope.ballistics.ui.screens.CameraCalibrationScreen
import za.co.dope.ballistics.ui.screens.ComparisonScreen
import za.co.dope.ballistics.ui.screens.DashboardScreen
import za.co.dope.ballistics.ui.screens.EnvironmentScreen
import za.co.dope.ballistics.ui.screens.MoreScreen
import za.co.dope.ballistics.ui.screens.ProfileEquipmentPreview
import za.co.dope.ballistics.ui.screens.ProfilesScreen
import za.co.dope.ballistics.ui.screens.RangeCardScreen
import za.co.dope.ballistics.ui.screens.ResultsScreen
import za.co.dope.ballistics.ui.screens.RifleScreen
import za.co.dope.ballistics.ui.screens.ScopeDetailScreen
import za.co.dope.ballistics.ui.screens.ScopeScreen
import za.co.dope.ballistics.ui.screens.SessionDraftState
import za.co.dope.ballistics.ui.screens.SessionScreen
import za.co.dope.ballistics.ui.screens.SetupDraftState
import za.co.dope.ballistics.ui.screens.SplashScreen
import za.co.dope.ballistics.ui.screens.TargetRangeScreen
import za.co.dope.ballistics.ui.screens.WindFormState
import za.co.dope.ballistics.ui.screens.WindScreen
import za.co.dope.ballistics.ui.screens.ZeroSetupScreen
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.DopeTheme
import za.co.dope.ballistics.ui.theme.DopeThemeMode

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val BottomDestinations =
    listOf(
        BottomDestination("dashboard", "Home", Icons.Outlined.Home),
        BottomDestination("profiles", "Profiles", Icons.Outlined.Inventory2),
        BottomDestination("environment", "Environment", Icons.Outlined.Public),
        BottomDestination("session", "Sessions", Icons.Outlined.TrackChanges),
        BottomDestination("more", "More", Icons.Outlined.MoreHoriz),
    )

@Composable
fun DopeApp(
    startRoute: String = "splash",
    profileRepository: ProfileRepository? = null,
    sessionRepository: SessionRepository? = null,
) {
    var themeModeName by rememberSaveable { mutableStateOf(DopeThemeMode.DARK.name) }
    val themeMode = DopeThemeMode.valueOf(themeModeName)
    DopeTheme(mode = themeMode) {
        val navController = rememberNavController()
        val windState = remember { WindFormState() }
        val setupDraftState = remember { SetupDraftState() }
        val sessionDraftState = remember { SessionDraftState() }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val currentRoute = currentDestination?.route
        val showBottomBar = currentRoute != null && currentRoute != "splash"
        val openRoute: (String) -> Unit = { route ->
            navController.navigate(route) { launchSingleTop = true }
        }

        Scaffold(
            contentWindowInsets =
                WindowInsets.safeDrawing.only(
                    androidx.compose.foundation.layout.WindowInsetsSides.Horizontal +
                        androidx.compose.foundation.layout.WindowInsetsSides.Top,
                ),
            bottomBar = {
                if (showBottomBar) {
                    DopeBottomNavigation(
                        selectedRoute = currentRoute,
                        onSelect = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo("dashboard") { saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            DopeNavHost(
                navController = navController,
                startRoute = startRoute,
                profileRepository = profileRepository,
                sessionRepository = sessionRepository,
                windState = windState,
                setupDraftState = setupDraftState,
                sessionDraftState = sessionDraftState,
                openRoute = openRoute,
                modifier = Modifier.padding(innerPadding),
                onThemeChange = {
                    themeModeName =
                        when (themeMode) {
                            DopeThemeMode.DARK -> DopeThemeMode.HIGH_CONTRAST.name
                            DopeThemeMode.HIGH_CONTRAST -> DopeThemeMode.RED_LIGHT.name
                            DopeThemeMode.RED_LIGHT -> DopeThemeMode.DARK.name
                        }
                },
            )
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun DopeNavHost(
    navController: NavHostController,
    startRoute: String,
    profileRepository: ProfileRepository?,
    sessionRepository: SessionRepository?,
    windState: WindFormState,
    setupDraftState: SetupDraftState,
    sessionDraftState: SessionDraftState,
    openRoute: (String) -> Unit,
    onThemeChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
    ) {
        composable("splash") {
            SplashScreen {
                navController.navigate("dashboard") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("dashboard") { DashboardScreen(profileRepository, setupDraftState, windState, openRoute) }
        composable("profiles") {
            val rifles by profileRepository?.observeRifles()?.collectAsState(emptyList()) ?: remember {
                mutableStateOf(emptyList())
            }
            val ammunition by
                profileRepository?.observeAmmunition()?.collectAsState(emptyList()) ?: remember {
                    mutableStateOf(emptyList())
                }
            val scopes by
                profileRepository?.observeScopeProfiles()?.collectAsState(emptyList()) ?: remember {
                    mutableStateOf(emptyList())
                }
            val zeros by
                profileRepository?.observeZeroProfiles()?.collectAsState(emptyList()) ?: remember {
                    mutableStateOf(emptyList())
                }
            ProfilesScreen(openRoute, rifles.size, ammunition.size, scopes.size, zeros.size)
        }
        composable("rifle") { RifleScreen(profileRepository) }
        composable("ammo") { AmmunitionScreen(profileRepository) }
        composable("scope") { ScopeScreen(openRoute, profileRepository) }
        composable("scope_detail") { ScopeDetailScreen(profileRepository) }
        composable("zero_setup") { ZeroSetupScreen(profileRepository, setupDraftState) }
        composable("environment") { EnvironmentScreen(openRoute, profileRepository) }
        composable("wind") { WindScreen(windState) }
        composable("results") {
            ResultsScreen(profileRepository, sessionRepository, windState, sessionDraftState, openRoute)
        }
        composable("range_card") { RangeCardScreen(profileRepository, windState, openRoute) }
        composable("session") {
            SessionScreen(profileRepository, sessionRepository, windState, sessionDraftState, openRoute)
        }
        composable("comparison") { ComparisonScreen(profileRepository, windState) }
        composable("camera_calibration") { CameraCalibrationScreen() }
        composable("target_range") { TargetRangeScreen(openRoute, profileRepository) }
        composable("more") { MoreScreen(onOpen = openRoute, onThemeChange = onThemeChange) }
    }
}

/**
 * Deterministic host for layoutlib golden tests. It renders the same production shells and
 * bottom navigation without requiring an Activity-owned navigation event dispatcher.
 */
@Composable
fun DopeGoldenScreen(
    route: String,
    themeMode: DopeThemeMode = DopeThemeMode.DARK,
) {
    DopeTheme(mode = themeMode) {
        val windState =
            remember {
                WindFormState().apply {
                    windFromDegrees = "312"
                    directionOfFireDegrees = "87"
                    minimumSpeedMps = "2.7"
                    averageSpeedMps = "4.5"
                    maximumSpeedMps = "6.3"
                    gustSpeedMps = "8.0"
                }
            }
        val sessionDraftState = remember { SessionDraftState() }
        Scaffold(
            contentWindowInsets =
                WindowInsets.safeDrawing.only(
                    androidx.compose.foundation.layout.WindowInsetsSides.Horizontal +
                        androidx.compose.foundation.layout.WindowInsetsSides.Top,
                ),
            bottomBar = {
                if (route != "splash") {
                    DopeBottomNavigation(selectedRoute = route, onSelect = {})
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (route) {
                    "splash" -> SplashScreen(onContinue = {})
                    "profiles" -> ProfilesScreen(onOpen = {})
                    "profile_visuals" -> ProfileEquipmentPreview()
                    "environment" -> EnvironmentScreen(onOpen = {}, previewMode = true)
                    "range_card" -> RangeCardScreen(null, windState, {}, previewMode = true)
                    "wind" -> WindScreen(windState)
                    "results" -> ResultsScreen(null, null, windState, sessionDraftState, {}, previewMode = true)
                    "session" -> SessionScreen(null, null, windState, sessionDraftState, {})
                    "comparison" -> ComparisonScreen(null, windState)
                    "target_range" -> TargetRangeScreen(onOpen = {})
                    else -> DashboardScreen(null, SetupDraftState(), windState, onOpen = {})
                }
            }
        }
    }
}

@Composable
private fun DopeBottomNavigation(
    selectedRoute: String?,
    onSelect: (BottomDestination) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = DopeDesignTokens.Sizing.BottomNavigationMinimum)
                .testTag("dope_bottom_navigation"),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.only(
                            androidx.compose.foundation.layout.WindowInsetsSides.Bottom,
                        ),
                    ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(DopeDesignTokens.Sizing.BottomNavigationContent),
            ) {
                BottomDestinations.forEach { destination ->
                    val selected = selectedRoute == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelect(destination) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.secondary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
        }
    }
}
