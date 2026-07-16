package com.yourcompany.agrolink.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import com.yourcompany.agrolink.repository.AgroLinkRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val trendingTags = listOf("tarım", "hasat2026", "toprak", "agroai", "sulama", "organik")

    val mockVisualGridItems = listOf(
        ExploreGridItem("1", "https://images.unsplash.com/photo-1560493676-04071c5f467b?w=400", "Biyolojik Mücadele", "tarım"),
        ExploreGridItem("2", "https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=400", "Toprak Organik Madde", "toprak"),
        ExploreGridItem("3", "https://images.unsplash.com/photo-1592417817098-8f3d6eb19675?w=400", "Damlama Boruları", "sulama"),
        ExploreGridItem("4", "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400", "Sabah Hasadı", "hasat2026"),
        ExploreGridItem("5", "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=400", "Agro AI ile Teşhis", "agroai"),
        ExploreGridItem("6", "https://images.unsplash.com/photo-1628352081506-83c43123ed6d?w=400", "Organik Tohum Çeşitleri", "organik"),
        ExploreGridItem("7", "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400", "Traktör Bakımı", "tarım"),
        ExploreGridItem("8", "https://images.unsplash.com/photo-1593113630400-ea4288922497?w=400", "Seralarda İklimlendirme", "agroai")
    )

    // Filter items based on search and tag
    val filteredItems = mockVisualGridItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() || item.title.lowercase().contains(searchQuery.lowercase()) || item.tag.lowercase().contains(searchQuery.lowercase())
        val matchesTag = selectedTag == null || item.tag.lowercase() == selectedTag!!.lowercase()
        matchesSearch && matchesTag
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Modern Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotBlank()) selectedTag = null
                    },
                    placeholder = { Text("Hasat, sulama, tohum, hastalık ara...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF111713),
                        unfocusedContainerColor = Color(0xFF111713)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Trending indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Popüler Başlıklar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                }

                // Horizontal scroll list of tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = selectedTag == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAllSelected) MaterialTheme.colorScheme.primary else Color(0xFF111713))
                                .clickable { selectedTag = null }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tümü",
                                color = if (isAllSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(trendingTags) { tag ->
                        val isSelected = selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF111713))
                                .clickable {
                                    selectedTag = if (isSelected) null else tag
                                    searchQuery = ""
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = tag,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                .padding(horizontal = 8.dp)
        ) {
            if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aramanızla eşleşen içerik bulunamadı.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ExploreGridCard(item)
                    }
                }
            }
        }
    }
}

data class ExploreGridItem(
    val id: String,
    val imageUrl: String,
    val title: String,
    val tag: String
)

@Composable
fun ExploreGridCard(item: ExploreGridItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111713))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // High fidelity drawing representing crop image with gradient card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F2C1A),
                                Color(0xFF08130C)
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${item.tag}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
