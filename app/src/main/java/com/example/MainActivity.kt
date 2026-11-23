package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirplaneTicket
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.rounded.AirplaneTicket
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.FirestoreKrisFlyerRepository
import com.example.domain.AppLanguage
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.EarnScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RedeemScreen
import com.example.ui.theme.KrisFlyerSimTheme
import com.example.ui.theme.KrisGold
import com.example.ui.viewmodel.KrisFlyerViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_HOME = "home"
const val ROUTE_EARN = "earn"
const val ROUTE_REDEEM = "redeem"
const val ROUTE_ACTIVITY = "activity"

private data class NavTabItem(
    val route: String,
    val labelVi: String,
    val labelEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navTabs = listOf(
    NavTabItem(
        route = ROUTE_HOME,
        labelVi = "Thẻ & Hạng",
        labelEn = "Card",
        selectedIcon = Icons.Rounded.CreditCard,
        unselectedIcon = Icons.Outlined.CreditCard
    ),
    NavTabItem(
        route = ROUTE_EARN,
        labelVi = "Tích Dặm",
        labelEn = "Earn",
        selectedIcon = Icons.Rounded.FlightTakeoff,
        unselectedIcon = Icons.Outlined.FlightTakeoff
    ),
    NavTabItem(
        route = ROUTE_REDEEM,
        labelVi = "Đổi Thưởng",
        labelEn = "Redeem",
        selectedIcon = Icons.Rounded.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard
    ),
    NavTabItem(
        route = ROUTE_ACTIVITY,
        labelVi = "Vé & Sao Kê",
        labelEn = "Activity",
        selectedIcon = Icons.Rounded.AirplaneTicket,
        unselectedIcon = Icons.Outlined.AirplaneTicket
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var checkedAutoSignIn by remember { mutableStateOf(auth.currentUser != null) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { checkedAutoSignIn = true },
                onUnauthenticated = { checkedAutoSignIn = true },
                scope = this
            )
        }
    }

    val user = currentUser
    if (!checkedAutoSignIn && user == null) {
        KrisFlyerSimTheme(darkTheme = true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KrisGold)
            }
        }
    } else if (user == null) {
        KrisFlyerSimTheme(darkTheme = true) {
            AuthScreen(
                onAuthSuccess = { /* authStateFlow updates currentUser automatically */ }
            )
        }
    } else {
        val uid = user.uid
        key(uid) {
            AuthenticatedKrisFlyerContent(
                currentUserId = uid,
                userDisplayName = user.displayName,
                userEmail = user.email,
                credentialManager = credentialManager
            )
        }
    }
}

@Composable
private fun AuthenticatedKrisFlyerContent(
    currentUserId: String,
    userDisplayName: String?,
    userEmail: String?,
    credentialManager: CredentialManager
) {
    val viewModel: KrisFlyerViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KrisFlyerViewModel(
                    repository = FirestoreKrisFlyerRepository(db),
                    currentUserId = currentUserId,
                    userDisplayName = userDisplayName,
                    userEmail = userEmail
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (uiState.profile?.darkMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }
    val scope = rememberCoroutineScope()

    KrisFlyerSimTheme(darkTheme = useDarkTheme) {
        KrisFlyerSimulatorApp(
            viewModel = viewModel,
            isDarkTheme = useDarkTheme,
            onSignOut = {
                signOutUser(credentialManager = credentialManager, scope = scope)
            }
        )
    }
}

@Composable
fun KrisFlyerSimulatorApp(
    viewModel: KrisFlyerViewModel,
    isDarkTheme: Boolean,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        navTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = if (uiState.language == AppLanguage.VI) tab.labelVi else tab.labelEn
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (uiState.language == AppLanguage.VI) tab.labelVi else tab.labelEn,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (uiState.errorMessage != null && uiState.profile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onSignOut) {
                            Text("Sign out & Retry")
                        }
                    }
                }
            } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KrisGold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.weight(0.2f))
                            navTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = if (uiState.language == AppLanguage.VI) tab.labelVi else tab.labelEn
                                        )
                                    },
                                    label = {
                                        Text(if (uiState.language == AppLanguage.VI) tab.labelVi else tab.labelEn)
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag("rail_tab_${tab.route}")
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                isDarkTheme = isDarkTheme,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onToggleDarkMode = { viewModel.setDarkMode(!isDarkTheme) },
                                onApplyPreset = { viewModel.applyPreset(it) },
                                onUpdateMemberName = { viewModel.updateMemberName(it) },
                                onNavigateToEarn = { navigateTab(ROUTE_EARN) },
                                onNavigateToRedeem = { navigateTab(ROUTE_REDEEM) },
                                onDismissCelebration = { viewModel.dismissTierCelebration() },
                                onSignOut = onSignOut
                            )
                        }

                        composable(ROUTE_EARN) {
                            EarnScreen(
                                uiState = uiState,
                                onSelectCarrier = { viewModel.updateEarnCarrier(it) },
                                onUpdateRoute = { o, d -> viewModel.updateEarnRoute(o, d) },
                                onSwapAirports = { viewModel.swapEarnAirports() },
                                onSelectCabin = { viewModel.updateEarnCabin(it) },
                                onToggleRoundTrip = { viewModel.updateEarnRoundTrip(it) },
                                onUpdateFareSgd = { viewModel.updateEarnFareSgd(it) },
                                onSimulateFlight = { viewModel.simulateFlightEarn() },
                                onSimulatePartner = { tv, te, sv, se, b, m ->
                                    viewModel.simulatePartnerEarn(tv, te, sv, se, b, m)
                                }
                            )
                        }

                        composable(ROUTE_REDEEM) {
                            RedeemScreen(
                                uiState = uiState,
                                onUpdateRoute = { o, d -> viewModel.updateRedeemRoute(o, d) },
                                onSwapAirports = { viewModel.swapRedeemAirports() },
                                onSelectCabin = { viewModel.updateRedeemCabin(it) },
                                onSelectAwardType = { viewModel.updateRedeemAwardType(it) },
                                onToggleRoundTrip = { viewModel.updateRedeemRoundTrip(it) },
                                onRedeemAwardFlight = {
                                    viewModel.redeemAwardFlight(
                                        onSuccess = { navigateTab(ROUTE_ACTIVITY) }
                                    )
                                },
                                onRedeemLifestyleVoucher = { tv, te, sv, se, b, c ->
                                    viewModel.redeemLifestyleVoucher(tv, te, sv, se, b, c)
                                }
                            )
                        }

                        composable(ROUTE_ACTIVITY) {
                            ActivityScreen(
                                uiState = uiState,
                                onAdvanceBookingStatus = { viewModel.advanceBookingStatus(it) },
                                onCancelAndRefundBooking = { viewModel.cancelAndRefundBooking(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
