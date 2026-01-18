package com.psm2.inva.presentation.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.psm2.inva.data.AuthRepository
import com.psm2.inva.presentation.features.add_blog.AddBlogScreen
import com.psm2.inva.presentation.features.add_blog.AddBlogViewModel
import com.psm2.inva.presentation.features.add_fish.AddFishScreen
import com.psm2.inva.presentation.features.add_fish.AddFishViewModel
import com.psm2.inva.presentation.features.blog_detail.BlogDetailScreen
import com.psm2.inva.presentation.features.blog_detail.BlogDetailViewModel
import com.psm2.inva.presentation.features.catch_detail.CatchDetailScreen
import com.psm2.inva.presentation.features.catch_detail.CatchDetailViewModel
import com.psm2.inva.presentation.features.classification.ClassificationResultScreen
import com.psm2.inva.presentation.features.classification.ClassificationResultViewModel
import com.psm2.inva.presentation.features.edit_blog.EditBlogScreen
import com.psm2.inva.presentation.features.edit_blog.EditBlogViewModel
import com.psm2.inva.presentation.features.edit_catch.EditCatchScreen
import com.psm2.inva.presentation.features.edit_catch.EditCatchViewModel
import com.psm2.inva.presentation.features.edit_fish.EditFishScreen
import com.psm2.inva.presentation.features.edit_fish.EditFishViewModel
import com.psm2.inva.presentation.features.edit_profile.EditProfileScreen
import com.psm2.inva.presentation.features.edit_profile.EditProfileViewModel
import com.psm2.inva.presentation.features.home.HomeScreen
import com.psm2.inva.presentation.features.home.HomeViewModel
import com.psm2.inva.presentation.features.identify.IdentifyScreen
import com.psm2.inva.presentation.features.identify.IdentifyViewModel
import com.psm2.inva.presentation.features.map.MapScreen
import com.psm2.inva.presentation.features.map.MapViewModel
import com.psm2.inva.presentation.features.profile.ProfileScreen
import com.psm2.inva.presentation.features.profile.ProfileViewModel
import com.psm2.inva.presentation.features.report.ReportScreen
import com.psm2.inva.presentation.features.report.ReportViewModel
import com.psm2.inva.presentation.features.scan.ScanFishScreen
import com.psm2.inva.presentation.features.sign_in.SignInScreen
import com.psm2.inva.presentation.features.sign_in.SignInViewModel
import com.psm2.inva.presentation.features.species_details.SpeciesDetailScreen
import com.psm2.inva.presentation.features.species_details.SpeciesDetailViewModel
import com.psm2.inva.presentation.features.stats.StatsScreen
import com.psm2.inva.presentation.features.stats.StatsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object MainTabs : Screen("main_tabs")
    object Report : Screen("report")
    object Stats : Screen("stats")
    object AddFish : Screen("add_fish")
    object ScanFish : Screen("scan_fish")
    object EditProfile : Screen("edit_profile")
    object SpeciesDetail : Screen("species_detail/{fishId}") {
        fun createRoute(fishId: String) = "species_detail/$fishId"
    }
    object EditFish : Screen("edit_fish/{fishId}") {
        fun createRoute(fishId: String) = "edit_fish/$fishId"
    }
    object ClassificationResult : Screen("classification_result/{imageUri}") {
        fun createRoute(imageUri: Uri): String {
            val encodedUri = URLEncoder.encode(imageUri.toString(), StandardCharsets.UTF_8.toString())
            return "classification_result/$encodedUri"
        }
    }
    object CatchDetail : Screen("catch_detail/{sightingId}") {
        fun createRoute(sightingId: String) = "catch_detail/$sightingId"
    }
    object EditCatch : Screen("edit_catch/{sightingId}") {
        fun createRoute(sightingId: String) = "edit_catch/$sightingId"
    }
    object AddBlog : Screen("add_blog/{categoryName}") {
        fun createRoute(categoryName: String) = "add_blog/$categoryName"
    }
    object EditBlog : Screen("edit_blog/{blogId}") {
        fun createRoute(blogId: String) = "edit_blog/$blogId"
    }
    object BlogDetail : Screen("blog_detail/{blogId}") {
        fun createRoute(blogId: String) = "blog_detail/$blogId"
    }
}

sealed class MainScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : MainScreen("home", "Home", Icons.Default.Home)
    object Identify : MainScreen("identify", "Identify", Icons.Default.Search)
    object Map : MainScreen("map", "Map", Icons.Default.LocationOn)
    object Profile : MainScreen("profile", "Profile", Icons.Default.Person)
}

val mainScreens = listOf(
    MainScreen.Home,
    MainScreen.Identify,
    MainScreen.Map,
    MainScreen.Profile
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val repository: AuthRepository = koinInject()
    val startDestination =
        if (repository.getSignedInUser() != null) Screen.MainTabs.route else Screen.SignIn.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.SignIn.route) {
            val signInViewModel = koinViewModel<SignInViewModel>()
            val state by signInViewModel.state.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(context, "Sign in successful", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                    signInViewModel.resetState()
                }
            }
            SignInScreen(
                state = state,
                onSignInClick = signInViewModel::signIn
            )
        }

        composable(Screen.MainTabs.route) {
            MainAppScaffold(rootNavController = navController)
        }

        composable(Screen.Report.route) {
            val reportViewModel = koinViewModel<ReportViewModel>()
            ReportScreen(
                viewModel = reportViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(Screen.Stats.route) {
            val statsViewModel = koinViewModel<StatsViewModel>()
            StatsScreen(
                viewModel = statsViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(Screen.AddFish.route) {
            val addFishViewModel = koinViewModel<AddFishViewModel>()
            AddFishScreen(
                viewModel = addFishViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(Screen.ScanFish.route) {
            ScanFishScreen(
                onImageCaptured = { uri ->
                    navController.navigate(Screen.ClassificationResult.createRoute(uri))
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(Screen.EditProfile.route) {
            val editProfileViewModel = koinViewModel<EditProfileViewModel>()
            EditProfileScreen(
                viewModel = editProfileViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.SpeciesDetail.route,
            arguments = listOf(navArgument("fishId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishId = backStackEntry.arguments?.getString("fishId") ?: ""
            val speciesDetailViewModel =
                koinViewModel<SpeciesDetailViewModel>(parameters = { parametersOf(fishId) })

            SpeciesDetailScreen(
                viewModel = speciesDetailViewModel,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditFish.createRoute(fishId))
                }
            )
        }

        composable(
            route = Screen.EditFish.route,
            arguments = listOf(navArgument("fishId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishId = backStackEntry.arguments?.getString("fishId") ?: ""
            val editFishViewModel =
                koinViewModel<EditFishViewModel>(parameters = { parametersOf(fishId) })

            EditFishScreen(
                viewModel = editFishViewModel,
                onNavigateUp = { navController.navigateUp() },
                onDeleteSuccess = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.MainTabs.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ClassificationResult.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) {
            val resultViewModel = koinViewModel<ClassificationResultViewModel>(
                parameters = { parametersOf(it.arguments) }
            )
            ClassificationResultScreen(
                viewModel = resultViewModel,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToDetails = { fishId ->
                    navController.navigate(Screen.SpeciesDetail.createRoute(fishId))
                }
            )
        }

        composable(
            route = Screen.CatchDetail.route,
            arguments = listOf(navArgument("sightingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sightingId = backStackEntry.arguments?.getString("sightingId") ?: ""
            val catchDetailViewModel =
                koinViewModel<CatchDetailViewModel>(parameters = { parametersOf(sightingId) })

            CatchDetailScreen(
                viewModel = catchDetailViewModel,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditCatch.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditCatch.route,
            arguments = listOf(navArgument("sightingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sightingId = backStackEntry.arguments?.getString("sightingId") ?: ""
            val editCatchViewModel = koinViewModel<EditCatchViewModel>(
                parameters = { parametersOf(sightingId) }
            )
            EditCatchScreen(
                viewModel = editCatchViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.AddBlog.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val addBlogViewModel = koinViewModel<AddBlogViewModel>(
                parameters = { parametersOf(categoryName) }
            )
            AddBlogScreen(
                viewModel = addBlogViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.EditBlog.route,
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val blogId = backStackEntry.arguments?.getString("blogId") ?: ""
            val editBlogViewModel = koinViewModel<EditBlogViewModel>(
                parameters = { parametersOf(blogId) }
            )
            EditBlogScreen(
                viewModel = editBlogViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.BlogDetail.route,
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val blogId = backStackEntry.arguments?.getString("blogId") ?: ""
            val blogDetailViewModel = koinViewModel<BlogDetailViewModel>(
                parameters = { parametersOf(blogId) }
            )
            BlogDetailScreen(
                viewModel = blogDetailViewModel,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditBlog.createRoute(id))
                }
            )
        }
    }
}

@Composable
fun MainAppScaffold(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    var isMenuExpanded by remember { mutableStateOf(false) }

    val authRepository: AuthRepository = koinInject()
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = authRepository.getSignedInUser()
        if (user != null) {
            isAdmin = authRepository.isAdmin(user.userId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Box(contentAlignment = Alignment.BottomCenter) {
                    AppBottomNavigationBar(
                        navController = mainNavController,
                        onFabClick = { isMenuExpanded = !isMenuExpanded }
                    )

                    FloatingActionButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier
                            .offset(y = (-32).dp)
                            .size(64.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        val rotation by animateFloatAsState(
                            targetValue = if (isMenuExpanded) 45f else 0f,
                            label = "fab_rotation"
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Actions",
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(rotation),
                            tint = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = mainNavController,
                startDestination = MainScreen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(MainScreen.Home.route) {
                    val homeViewModel = koinViewModel<HomeViewModel>()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToBlogDetail = { blogId ->
                            rootNavController.navigate(Screen.BlogDetail.createRoute(blogId))
                        },
                        onNavigateToCatchDetail = { sightingId ->
                            rootNavController.navigate(Screen.CatchDetail.createRoute(sightingId))
                        },
                        onNavigateToSpeciesDetail = { fishId ->
                            rootNavController.navigate(Screen.SpeciesDetail.createRoute(fishId))
                        },
                        onNavigateToStats = {
                            rootNavController.navigate(Screen.Stats.route)
                        }
                    )
                }

                composable(MainScreen.Map.route) {
                    val mapViewModel = koinViewModel<MapViewModel>()
                    val state by mapViewModel.state.collectAsState()
                    MapScreen(
                        state = state,
                        viewModel = mapViewModel,
                        onNavigateToReport = {
                            rootNavController.navigate(Screen.Report.route)
                        },
                        onNavigateToCatchDetail = { sightingId ->
                            rootNavController.navigate(Screen.CatchDetail.createRoute(sightingId))
                        }
                    )
                }

                composable(MainScreen.Identify.route) {
                    val identifyViewModel = koinViewModel<IdentifyViewModel>()
                    val state by identifyViewModel.state.collectAsState()
                    IdentifyScreen(
                        state = state,
                        onNavigateToDetails = { fishId ->
                            rootNavController.navigate(Screen.SpeciesDetail.createRoute(fishId))
                        },
                        onNavigateToClassification = { uri ->
                            rootNavController.navigate(Screen.ClassificationResult.createRoute(uri))
                        },
                        onNavigateToAddFish = {
                            rootNavController.navigate(Screen.AddFish.route)
                        }
                    )
                }
                composable(MainScreen.Profile.route) {
                    val profileViewModel = koinViewModel<ProfileViewModel>()
                    val state by profileViewModel.state.collectAsState()
                    ProfileScreen(
                        state = state,
                        onSignOut = {
                            profileViewModel.onSignOut()
                            rootNavController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.MainTabs.route) { inclusive = true }
                            }
                        },
                        onNavigateToCatchDetail = { sightingId ->
                            rootNavController.navigate(Screen.CatchDetail.createRoute(sightingId))
                        },
                        onNavigateToEditProfile = {
                            rootNavController.navigate(Screen.EditProfile.route)
                        }
                    )
                }
            }
        }

        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isMenuExpanded = false }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isAdmin) {
                    MenuItem(
                        icon = Icons.Default.Article,
                        text = "Write Article",
                        onClick = {
                            isMenuExpanded = false
                            rootNavController.navigate(Screen.AddBlog.createRoute("General"))
                        }
                    )
                }

                MenuItem(
                    icon = Icons.Default.PhotoCamera,
                    text = "Scan Fish",
                    onClick = {
                        isMenuExpanded = false
                        rootNavController.navigate(Screen.ScanFish.route)
                    }
                )

                MenuItem(
                    icon = Icons.Default.AddPhotoAlternate,
                    text = "Log Catch",
                    onClick = {
                        isMenuExpanded = false
                        rootNavController.navigate(Screen.Report.route)
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVerticallyAndFadeIn(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onClick() }
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

fun slideInVerticallyAndFadeIn() = expandVertically(expandFrom = Alignment.Bottom) + fadeIn()

@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    onFabClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val leftScreens = listOf(MainScreen.Home, MainScreen.Identify)
        leftScreens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        NavigationBarItem(
            selected = false,
            onClick = onFabClick,
            icon = {},
            enabled = false
        )

        val rightScreens = listOf(MainScreen.Map, MainScreen.Profile)
        rightScreens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}