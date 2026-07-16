package com.yourcompany.agrolink.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.agrolink.model.*
import com.yourcompany.agrolink.repository.AgroLinkRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroModulesScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Çiftlik Defteri", "Hasat Takip", "Mağaza", "Borsa", "Alarmlar", "Hava Durumu")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Text(
                    text = "Tarım Portalı",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // Scrollable tab row with glowing line indicator
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .padding(horizontal = 16.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> FarmbookTab()
                1 -> HarvestTrackerTab()
                2 -> MarketplaceTab()
                3 -> MarketPricesTab()
                4 -> DiseaseAlarmsTab()
                5 -> WeatherTab()
            }
        }
    }
}

// ==========================================
// 1. Farmbook Tab (Çiftlik Defteri)
// ==========================================
@Composable
fun FarmbookTab() {
    val records by AgroLinkRepository.farmRecords.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalIncome = records.sumOf { it.income }
    val totalCost = records.sumOf { it.cost }
    val totalProfit = totalIncome - totalCost

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Financial summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Finansal Bakiye (Cari)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%,.2f", totalProfit)} TL",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (totalProfit >= 0) MaterialTheme.colorScheme.primary else Color.Red
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Toplam Gider", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("${String.format("%,.0f", totalCost)} TL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Toplam Gelir", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("${String.format("%,.0f", totalIncome)} TL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Animated drawn statistics Canvas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mali Gelir-Gider Dağılımı", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Calculate relative ratio
                        val maxTotal = (totalIncome + totalCost).coerceAtLeast(1.0)
                        val incomeWidth = (totalIncome / maxTotal * width).toFloat()
                        val costWidth = (totalCost / maxTotal * width).toFloat()

                        // Draw background track
                        drawRoundRect(
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )

                        // Draw income bar green
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF34D399))),
                            size = androidx.compose.ui.geometry.Size(incomeWidth, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )

                        // Draw cost bar overlay from right
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFF87171))),
                            topLeft = Offset(width - costWidth, 0f),
                            size = androidx.compose.ui.geometry.Size(costWidth, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gelir oranı: %${String.format("%.0f", (totalIncome / (totalIncome + totalCost).coerceAtLeast(1.0) * 100))}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Gider oranı: %${String.format("%.0f", (totalCost / (totalIncome + totalCost).coerceAtLeast(1.0) * 100))}", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Header and Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cari Kayıtlar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kayıt Ekle", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz muhasebe kaydı bulunmamaktadır.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(records) { record ->
                FarmRecordCard(
                    record = record,
                    onDelete = { AgroLinkRepository.deleteFarmRecord(record.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddFarmRecordDialog(
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun FarmRecordCard(record: FarmRecord, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (record.recordType == "Gelir") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.recordType == "Gelir") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (record.recordType == "Gelir") MaterialTheme.colorScheme.primary else Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(record.productName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${record.fieldName} • ${record.recordDate}", color = Color.Gray, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                val value = if (record.recordType == "Gelir") record.income else record.cost
                Text(
                    text = "${if (record.recordType == "Gelir") "+" else "-"}${String.format("%,.0f", value)} TL",
                    color = if (record.recordType == "Gelir") MaterialTheme.colorScheme.primary else Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun AddFarmRecordDialog(onDismiss: () -> Unit) {
    var type by remember { mutableStateOf("Gider") }
    var name by remember { mutableStateOf("") }
    var field by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var costIncome by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Muhasebe Kaydı", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type Select
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == "Gider") Color.Red else Color.Transparent)
                            .clickable { type = "Gider" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gider", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == "Gelir") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { type = "Gelir" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gelir", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ürün/Hizmet Adı (Örn: Mazot Alımı)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = field,
                    onValueChange = { field = it },
                    label = { Text("Tarla Adı (Örn: Karatay Merkez)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costIncome,
                    onValueChange = { costIncome = it },
                    label = { Text("Tutar (TL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || costIncome.isBlank()) return@TextButton
                    val costValue = if (type == "Gider") costIncome.toDoubleOrNull() ?: 0.0 else 0.0
                    val incomeValue = if (type == "Gelir") costIncome.toDoubleOrNull() ?: 0.0 else 0.0

                    AgroLinkRepository.addFarmRecord(
                        FarmRecord(
                            id = UUID.randomUUID().toString(),
                            recordType = type,
                            productName = name,
                            fieldName = field,
                            cost = costValue,
                            income = incomeValue,
                            recordDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
                            season = "Yaz",
                            year = 2026
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Ekle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

// ==========================================
// 2. Harvest Tracker Tab (Hasat Takip)
// ==========================================
@Composable
fun HarvestTrackerTab() {
    val trackers by AgroLinkRepository.fieldTrackers.collectAsState()
    var showAddTracker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tarlalarım ve Hasat Takvimi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Hasat olgunlaşma aşamaları ve gelişim takibi", color = Color.Gray, fontSize = 11.sp)
                }
                Button(
                    onClick = { showAddTracker = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yeni Tarla", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(trackers) { tracker ->
            FieldTrackerCard(tracker)
        }
    }

    if (showAddTracker) {
        AddTrackerDialog(onDismiss = { showAddTracker = false })
    }
}

@Composable
fun FieldTrackerCard(tracker: FieldTracker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(tracker.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${tracker.product} • ${tracker.alanDonm} Dönüm", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Hasat: ${tracker.tahminiHasat}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar representing days till harvest
            val percentVal = 0.75f // Simulated
            val progressAnimate by animateFloatAsState(targetValue = percentVal)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Olgunlaşma Seviyesi", color = Color.Gray, fontSize = 11.sp)
                    Text("%${(progressAnimate * 100).toInt()}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressAnimate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.DarkGray.copy(alpha = 0.3f)
                )
            }

            if (tracker.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 Son Not: ${tracker.notes.last()}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AddTrackerDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var sizeText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Tarla Hasat Takibi", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tarla Adı (Örn: Çayır Mevkii)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Ekilen Ürün (Örn: Ayçiçeği)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sizeText,
                    onValueChange = { sizeText = it },
                    label = { Text("Alan (Dönüm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || crop.isBlank()) return@TextButton
                    AgroLinkRepository.addFieldTracker(
                        FieldTracker(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            product = crop,
                            alanDonm = sizeText.toDoubleOrNull() ?: 10.0,
                            tahminiHasat = "20.09.2026",
                            notes = listOf("Tarla sisteme kaydedildi.")
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Ekle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

// ==========================================
// 3. Marketplace Tab (Mağaza)
// ==========================================
@Composable
fun MarketplaceTab() {
    val products by AgroLinkRepository.products.collectAsState()
    var searchKey by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }

    val categories = listOf("Tohum", "Gübre", "İlaç", "Makine", "Mahsul")

    val filteredProducts = products.filter {
        val matchesSearch = searchKey.isBlank() || it.name.lowercase().contains(searchKey.lowercase())
        val matchesCat = selectedCategory == null || it.category == selectedCategory
        matchesSearch && matchesCat
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Category row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchKey,
                onValueChange = { searchKey = it },
                placeholder = { Text("Ürün veya ekipman ara...", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111713),
                    unfocusedContainerColor = Color(0xFF111713)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { showAddProduct = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Horizontal Category Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory == null) MaterialTheme.colorScheme.primary else Color(0xFF111713))
                        .clickable { selectedCategory = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Tümü", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF111713))
                        .clickable { selectedCategory = if (isSelected) null else cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Classified Results
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (filteredProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text("Eşleşen ilan bulunamadı.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    ProductItemCard(product)
                }
            }
        }
    }

    if (showAddProduct) {
        AddProductDialog(onDismiss = { showAddProduct = false })
    }
}

@Composable
fun ProductItemCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // High fidelity cover representation
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF065F46), Color(0xFF0F172A))
                        ),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(product.category, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Satan: ${product.sellerName} • ${product.location}", color = Color.Gray, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%,.0f", product.price)} TL",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Stok: ${product.stock.toInt()} ${product.stockUnit}", color = Color.LightGray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Tohum") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Satılık İlanı", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("İlan Başlığı") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Tutar (TL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Detaylı Açıklama") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || priceText.isBlank()) return@TextButton
                    AgroLinkRepository.addProduct(
                        Product(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = desc,
                            price = priceText.toDoubleOrNull() ?: 100.0,
                            category = cat,
                            stock = 1.0,
                            sellerName = "Ahmet Bostancı",
                            sellerId = "me_user_123"
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Yayınla", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

// ==========================================
// 4. Market Prices Tab (Borsa Fiyatları)
// ==========================================
@Composable
fun MarketPricesTab() {
    val prices by AgroLinkRepository.cropPrices.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column {
                Text("Borsa Fiyat Takipçisi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Türkiye geneli ticaret borsaları anlık ürün tavan-taban fiyatları", color = Color.Gray, fontSize = 11.sp)
            }
        }

        items(prices) { price ->
            CropPriceRow(price)
        }
    }
}

@Composable
fun CropPriceRow(price: CropPrice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(price.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${price.market} • Birim: ${price.unit}", color = Color.Gray, fontSize = 11.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                    Text(price.priceRange, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isUp = price.changeRate >= 0
                        Icon(
                            imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isUp) MaterialTheme.colorScheme.primary else Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${if (isUp) "+" else ""}${price.changeRate}%",
                            color = if (isUp) MaterialTheme.colorScheme.primary else Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { AgroLinkRepository.toggleFollowPrice(price.id) },
                    modifier = Modifier
                        .background(if (price.isFollowed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (price.isFollowed) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Takip",
                        tint = if (price.isFollowed) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. Disease Alarms (Hastalık Alarmları)
// ==========================================
@Composable
fun DiseaseAlarmsTab() {
    val alarms by AgroLinkRepository.diseaseAlarms.collectAsState()
    var showAddAlarm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bölgesel Salgın Hastalık Alarmları", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Bölgenizdeki bitki hastalıklarına karşı acil bültenler", color = Color.Gray, fontSize = 11.sp)
                }
                IconButton(
                    onClick = { showAddAlarm = true },
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Alarm Ekle", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }

        items(alarms) { alarm ->
            DiseaseAlarmCard(alarm)
        }
    }

    if (showAddAlarm) {
        AddAlarmDialog(onDismiss = { showAddAlarm = false })
    }
}

@Composable
fun DiseaseAlarmCard(alarm: DiseaseAlarm) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(alarm.cropType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(alarm.urgency, color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(alarm.diseaseInfo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(alarm.note, color = Color.LightGray, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp))

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📍 Bölge: ${alarm.city}, ${alarm.district}", color = Color.Gray, fontSize = 10.sp)
                Text("Bildiren: ${alarm.reportedBy}", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AddAlarmDialog(onDismiss: () -> Unit) {
    var crop by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Hastalık İhbarı Bildir", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Etkilenen Mahsul (Örn: Buğday)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = disease,
                    onValueChange = { disease = it },
                    label = { Text("Hastalık/Zararlı Adı (Örn: Pas)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Şehir/İlçe (Örn: Konya, Karatay)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Gözlemlenen Hasar ve Tavsiye Notu") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (crop.isBlank() || disease.isBlank()) return@TextButton
                    AgroLinkRepository.addDiseaseAlarm(
                        DiseaseAlarm(
                            id = UUID.randomUUID().toString(),
                            cropType = crop,
                            diseaseInfo = disease,
                            city = city.substringBefore(",").trim(),
                            district = city.substringAfter(",", "Merkez").trim(),
                            note = desc,
                            reportedBy = "Ahmet Bostancı",
                            reportDate = "Bugün"
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("İhbar Et", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1313),
        shape = RoundedCornerShape(16.dp)
    )
}

// ==========================================
// 6. Weather Tab (Çiftçi Hava Durumu)
// ==========================================
@Composable
fun WeatherTab() {
    val weather by AgroLinkRepository.weather.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        weather?.let { w ->
            // Current Weather Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(w.city, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(w.condition, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${w.temperature}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nem", color = Color.Gray, fontSize = 11.sp)
                                Text("%${w.humidity}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rüzgar", color = Color.Gray, fontSize = 11.sp)
                                Text("${w.windSpeed} km/s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hava Kalitesi", color = Color.Gray, fontSize = 11.sp)
                                Text(w.airQuality, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Agro recommendation card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AgroLink Ziraat Tavsiyesi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = w.recommendation,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
