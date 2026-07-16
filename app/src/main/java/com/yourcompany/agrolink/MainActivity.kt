package com.yourcompany.agrolink

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.agrolink.repository.AgroLinkRepository
import com.yourcompany.agrolink.ui.screens.*
import com.yourcompany.agrolink.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AgroLinkRepository.initialize(applicationContext)
        setContent {
            MyApplicationTheme {
                AgroLinkApp()
            }
        }
    }
}

sealed class Screen(val title: String, val icon: ImageVector, val activeIcon: ImageVector) {
    object Feed : Screen("Ana Sayfa", Icons.Outlined.Home, Icons.Filled.Home)
    object Explore : Screen("Keşfet", Icons.Outlined.Search, Icons.Filled.Search)
    object CreatePost : Screen("Paylaş", Icons.Outlined.AddCircle, Icons.Filled.AddCircle)
    object AgroAI : Screen("Agro AI", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
    object Portal : Screen("Tarım", Icons.Outlined.Eco, Icons.Filled.Eco)
    object Profile : Screen("Profil", Icons.Outlined.Person, Icons.Filled.Person)
}












@Composable
fun AgroLinkApp() {
    val currentUser by AgroLinkRepository.currentUser.collectAsState()
    var currentTab by remember { mutableStateOf<Screen>(Screen.Feed) }
    var inDMs by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    GlobalScope.launch { AgroLinkRepository.updateDeviceToken(task.result) }
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            GlobalScope.launch { AgroLinkRepository.updateDeviceToken(task.result) }
                        }
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch { AgroLinkRepository.updateDeviceToken(task.result) }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentUser == null) {
            // Authentication and Onboarding Screens
            AuthScreen(onLoginSuccess = {
                currentTab = Screen.Feed
                inDMs = false
            })
        } else if (showSettings) {
            SettingsScreen(onBack = { showSettings = false })
        } else if (inDMs) {
            // Direct Messages inbox/chat views
            DirectMessagesScreen(onBack = { inDMs = false })
        } else {
            // Main App Dashboard Frame
            Scaffold(
                bottomBar = {
                    AgroBottomNavigation(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Fluid animated transition between tab screens
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                        },
                        label = "TabContent"
                    ) { tab ->
                        when (tab) {
                            Screen.Feed -> FeedScreen(
                                onNavigateToDMs = { inDMs = true },
                                onNavigateToProfile = { userId -> currentTab = Screen.Profile }
                            )
                            Screen.Explore -> ExploreScreen()
                            Screen.CreatePost -> CreatePostScreen(
                                onPostCreated = { currentTab = Screen.Feed }
                            )
                            Screen.AgroAI -> AgroAIChatScreen()
                            Screen.Portal -> AgroModulesScreen()
                            Screen.Profile -> ProfileScreen(onNavigateToSettings = { showSettings = true })
                        }
                    }
                }
            }
        }
    }
}

// Custom Premium Glassmorphic Bottom Navigation
@Composable
fun AgroBottomNavigation(
    selectedTab: Screen,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        Screen.Feed,
        Screen.Explore,
        Screen.CreatePost,
        Screen.AgroAI,
        Screen.Portal,
        Screen.Profile
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111713).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = selectedTab == screen
                val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onTabSelected(screen) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(if (screen == Screen.CreatePost) 42.dp else 28.dp)
                            .background(
                                if (screen == Screen.CreatePost) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF059669), Color(0xFF34D399))
                                    )
                                } else SolidColor(Color.Transparent),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.activeIcon else screen.icon,
                            contentDescription = screen.title,
                            tint = if (screen == Screen.CreatePost) Color.White else iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (screen != Screen.CreatePost) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}
