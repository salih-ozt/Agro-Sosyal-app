package com.yourcompany.agrolink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.agrolink.repository.AgroLinkRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var isPrivate by remember { mutableStateOf(false) }
    var receiveNotifications by remember { mutableStateOf(true) }
    
    // Server URL configuration state
    var serverUrl by remember { mutableStateOf("https://www.sehitumitkestitarimmtal.com") }
    var showUrlEdit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section Header Helper
            @Composable
            fun SectionHeader(title: String) {
                Text(
                    text = title.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Section 1: Account & Privacy
            Column {
                SectionHeader("HESAP VE GİZLİLİK")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111613)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gizli Hesap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Sadece onayladığınız takipçiler paylaşımlarınızı görebilir.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Anlık Bildirimler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Post beğenileri, yorumlar ve duyurulardan haberdar olun.", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = receiveNotifications,
                                onCheckedChange = { receiveNotifications = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            // Section 2: Server Connection Configuration
            Column {
                SectionHeader("SUNUCU BAĞLANTISI")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111613)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("API Sunucu Adresi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(serverUrl, color = Color.Gray, fontSize = 11.sp)
                            }
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kilitli",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Section 3: Active Device Sessions
            Column {
                SectionHeader("AKTİF CİHAZ OTURUMLARI")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111613)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SessionRow(deviceName = "Xiaomi Redmi Note 12 Pro (Bu Cihaz)", location = "Konya • Aktif", isActive = true)
                        SessionRow(deviceName = "Google Pixel 8", location = "Bursa • 2 gün önce", isActive = false)
                    }
                }
            }

            // Section 4: About Info
            Column {
                SectionHeader("HAKKINDA")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111613)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AgroLink Premium", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Versiyon 2.1.0 • Tüm hakları saklıdır", color = Color.Gray, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // Section 5: Logout Action
            Button(
                onClick = {
                    AgroLinkRepository.logout()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SessionRow(deviceName: String, location: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(deviceName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(location, color = Color.Gray, fontSize = 11.sp)
        }
    }
}
