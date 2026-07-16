package com.yourcompany.agrolink.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.agrolink.model.Post
import com.yourcompany.agrolink.model.User
import com.yourcompany.agrolink.repository.AgroLinkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateToSettings: () -> Unit) {
    val currentUser by AgroLinkRepository.currentUser.collectAsState()
    val posts by AgroLinkRepository.posts.collectAsState()
    val myPosts by AgroLinkRepository.myPosts.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showQuiz by remember { mutableStateOf(false) }
    var showPrivacySettings by remember { mutableStateOf(false) }

    var selectedGridTab by remember { mutableStateOf(0) } // 0 = Posts, 1 = Saved

    val savedPosts = posts.filter { it.isSaved }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AgroLinkRepository.fetchLiveData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentUser?.username ?: "Profil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = Color.LightGray)
                    }
                    IconButton(onClick = { AgroLinkRepository.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Çıkış Yap", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            currentUser?.let { user ->
                // Cover and Profile Photo Stack
                Box(modifier = Modifier.height(180.dp)) {
                    // Cover photo brush drawing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF064E3B), Color(0xFF022C22))
                                )
                            )
                    ) {
                        if (user.coverPic.isNotBlank()) {
                            AsyncImage(
                                model = user.coverPic,
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Profile pic positioning
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 80.dp)
                            .size(90.dp)
                            .background(Color(0xFF090D0A), CircleShape)
                            .padding(3.dp)
                    ) {
                        if (user.profilePic.isNotBlank()) {
                            AsyncImage(
                                model = user.profilePic,
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF047857), Color(0xFF10B981))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.username.take(2).uppercase(),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Name & Bio section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                        }
                        if (user.hasFarmerBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Altın Çiftçi",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Yellow.copy(alpha = 0.15f), CircleShape)
                            )
                        }
                    }

                    Text("@${user.username}", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                    Text(
                        text = user.bio,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Metadata details
                    if (user.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user.location, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    if (user.website.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user.website, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Counters row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileCounterColumn(label = "Gönderi", count = myPosts.size)
                        ProfileCounterColumn(label = "Takipçi", count = user.followerCount)
                        ProfileCounterColumn(label = "Takip", count = user.followingCount)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Certificate Application Trigger Banner
                    if (!user.hasFarmerBadge) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { showQuiz = true },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A0F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Altın Çiftçi Sertifikası Alın! 🏆", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tarımsal bilgi testini çözün, profilinize tescilli altın rozeti ekleyin.", color = Color.Gray, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Edit Profile Button
                    Button(
                        onClick = { showEditProfile = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111713))
                    ) {
                        Text("Profili Düzenle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Grid Switch tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111713), RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedGridTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedGridTab = 0 }
                               .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gönderilerim", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedGridTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedGridTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Kaydedilenler", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Post list/grid rendering
                    val targetList = if (selectedGridTab == 0) myPosts else savedPosts

                    if (targetList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedGridTab == 0) "Henüz bir paylaşım yapmadınız." else "Kaydedilmiş gönderi bulunmuyor.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Display list of matches with full modern cards
                        targetList.forEach { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (post.mediaUrls.isNotEmpty()) {
                                        AsyncImage(
                                            model = post.mediaUrls.first(),
                                            contentDescription = "Post Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .padding(bottom = 8.dp)
                                        )
                                    }
                                    Text(post.content, color = Color.LightGray, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(post.locationName.ifBlank { "Genel" }, color = Color.Gray, fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${post.likeCount}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${post.commentCount}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // Edit Profile Sheet
        if (showEditProfile) {
            EditProfileDialog(onDismiss = { showEditProfile = false })
        }

        // Privacy Management Dialog
        if (showPrivacySettings) {
            PrivacySettingsDialog(onDismiss = { showPrivacySettings = false })
        }

        // Quiz Overlay
        if (showQuiz) {
            QuizOverlay(onDismiss = { showQuiz = false })
        }
    }
}

@Composable
fun ProfileCounterColumn(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EditProfileDialog(onDismiss: () -> Unit) {
    val current = AgroLinkRepository.currentUser.collectAsState().value ?: return
    var name by remember { mutableStateOf(current.name) }
    var bio by remember { mutableStateOf(current.bio) }
    var location by remember { mutableStateOf(current.location) }
    var website by remember { mutableStateOf(current.website) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var profilePicUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        profilePicUri = uri
    }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profili Düzenle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Profile Photo Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        if (profilePicUri != null) {
                            AsyncImage(
                                model = profilePicUri,
                                contentDescription = "New Profile Pic",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else if (current.profilePic.isNotBlank()) {
                            AsyncImage(
                                model = current.profilePic,
                                contentDescription = "Profile Pic",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Gray, CircleShape))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Fotoğraf Seç", fontSize = 11.sp, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { AgroLinkRepository.deleteProfilePicture("profile") },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Fotoğrafı Sil", color = Color.Red, fontSize = 10.sp)
                            }
                            TextButton(
                                onClick = { AgroLinkRepository.deleteProfilePicture("cover") },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Kapağı Sil", color = Color.Red, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("İsim Soyisim") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biyografi") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Konum") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Websitesi") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        var base64 = ""
                        profilePicUri?.let { uri ->
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                base64 = uriToBase64(context, uri)
                            }
                        }
                        AgroLinkRepository.updateProfile(name, bio, location, website, profilePicBase64 = base64)
                        onDismiss()
                    }
                }
            ) {
                Text("Kaydet", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF131A15),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PrivacySettingsDialog(onDismiss: () -> Unit) {
    var isPrivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gizlilik ve Oturumlar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gizli Hesap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Sadece onayladığınız takipçiler görebilir.", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                Text("Aktif Cihaz Oturumları", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SessionRow(deviceName = "Xiaomi Redmi Note 12 Pro (Bu Cihaz)", location = "Konya • Aktif", isActive = true)
                    SessionRow(deviceName = "Google Pixel 8", location = "Bursa • 2 gün önce", isActive = false)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF131A15),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SessionRow(deviceName: String, location: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(deviceName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(location, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

// Gamified Golden Badge Quiz
@Composable
fun QuizOverlay(onDismiss: () -> Unit) {
    var quizStep by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var quizResultSuccess by remember { mutableStateOf(false) }

    val quizQuestions = listOf(
        QuizQuestion(
            "Mısır tarımında en yüksek verimi almak için taban gübrelemesinde hangi besin elementi önceliklidir?",
            listOf("Kalsiyum (Ca)", "Fosfor (P)", "Demir (Fe)", "Potasyum (K)"),
            1 // Fosfor
        ),
        QuizQuestion(
            "Sarı pas hastalığı (Puccinia striiformis) en çok hangi hava koşulunda hızlı yayılır?",
            listOf("Sıcak ve Kuru (Çöl havası)", "Dondurucu soğuk", "Serin ve Nemli (İlkbahar yağışları)", "Kurak rüzgarlar"),
            2 // Serin ve Nemli
        ),
        QuizQuestion(
            "Toprağın su tutma kapasitesini ve mikroorganizma faaliyetlerini doğal yollarla artıran gübre hangisidir?",
            listOf("Katı Üre Gübresi", "Organik Solucan Gübresi", "Amonyum Nitrat", "Kükürt tozu"),
            1 // Solucan gübresi
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A16))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (quizStep < quizQuestions.size) {
                    val q = quizQuestions[quizStep]

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Altın Çiftçi Sınavı (${quizStep + 1}/${quizQuestions.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = q.question,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    q.options.forEachIndexed { index, opt ->
                        val isChosen = selectedAnswer == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedAnswer = index }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Vazgeç", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                if (selectedAnswer == q.correctIndex) {
                                    if (quizStep == quizQuestions.size - 1) {
                                        // Completed and succeeded
                                        AgroLinkRepository.applyForVerification()
                                        quizResultSuccess = true
                                        quizStep++
                                    } else {
                                        quizStep++
                                        selectedAnswer = null
                                    }
                                } else {
                                    // Wrong answer, fail
                                    selectedAnswer = null
                                    quizStep = 99 // Trigger failure screen
                                }
                            },
                            enabled = selectedAnswer != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Sonraki Soru", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (quizStep == 99) {
                    // Fail screen
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Maalesef Yanlış Cevap!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Tarımsal bilim teorisini tazeleyip daha sonra tekrar deneyebilirsiniz.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            quizStep = 0
                            selectedAnswer = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yeniden Başla", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Success screen
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Tebrikler! Sınavı Geçtiniz 🎉", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Profilinize Altın Çiftçi Sertifikası ve Mavi Tik tescil edildi. Artık gönderilerinizde rozetiniz gözükecek!", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Rozetimi Göster", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
