package com.yourcompany.agrolink.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yourcompany.agrolink.model.Post
import com.yourcompany.agrolink.model.UserType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onVoteClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    var heartPulseVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111713)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onAuthorClick(post.userId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF065F46), Color(0xFF10B981))
                            ), CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.authorProfilePic.isNotBlank()) {
                        AsyncImage(
                            model = post.authorProfilePic,
                            contentDescription = "Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = post.authorUsername.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (post.hasVerifiedBadge) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        // Tag denoting type
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    when (post.authorUserType) {
                                        UserType.ENGINEER -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                        UserType.ACADEMIC -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = post.authorUserType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (post.authorUserType) {
                                    UserType.ENGINEER -> Color(0xFF60A5FA)
                                    UserType.ACADEMIC -> Color(0xFFA78BFA)
                                    else -> Color(0xFF34D399)
                                }
                            )
                        }
                    }
                    if (post.locationName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(post.locationName, color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
            }

            // Content text with highlighting tags
            val annotatedContent = buildAnnotatedString {
                val words = post.content.split(" ")
                words.forEachIndexed { i, word ->
                    if (word.startsWith("#") || word.startsWith("@")) {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                            append(word)
                        }
                    } else {
                        append(word)
                    }
                    if (i < words.size - 1) append(" ")
                }
            }

            Text(
                text = annotatedContent,
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            // Visual Media Body or Poll Card
            if (post.isPoll) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .background(Color(0xFF090D0A), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = post.pollQuestion,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val totalVotes = post.pollOptions.sumOf { it.votes }.coerceAtLeast(1)
                    val userHasVoted = post.pollOptions.any { it.isVoted }

                    post.pollOptions.forEach { option ->
                        val percent = (option.votes.toFloat() / totalVotes * 100).toInt()
                        val animatedPercent by animateFloatAsState(targetValue = percent.toFloat())

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onVoteClick(option.id) }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Animated percentage fill bar
                            if (userHasVoted) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedPercent / 100f)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                        .align(Alignment.CenterStart)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (option.isVoted) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = option.text,
                                        color = if (option.isVoted) MaterialTheme.colorScheme.primary else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (option.isVoted) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (userHasVoted) {
                                    Text(
                                        text = "$percent%",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toplam $totalVotes Oy • Aktif Anket",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (post.mediaUrls.isNotEmpty()) {
                // Large picture display with gesture tracking for Double-Tap Heart animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(top = 8.dp)
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (!post.isLiked) {
                                        onLikeClick()
                                    }
                                    heartPulseVisible = true
                                    coroutineScope.launch {
                                        delay(800)
                                        heartPulseVisible = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val mediaUrl = post.mediaUrls.firstOrNull() ?: ""
                    if (mediaUrl.isNotBlank()) {
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Post Media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF0F2F1D), Color(0xFF0D1D13))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "AgroLink Görsel Medya",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Large Heart Scale Overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = heartPulseVisible,
                        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }

            // Actions Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else Color.LightGray
                        )
                    }
                    IconButton(onClick = onCommentClick) {
                        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "Comment", tint = Color.LightGray)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = Color.LightGray)
                    }
                }

                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                }
            }

            // Likes and description summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${post.likeCount} Beğeni",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                if (post.comments.isNotEmpty()) {
                    Text(
                        text = "${post.commentCount} yorumun tümünü gör",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onCommentClick() }
                            .padding(vertical = 4.dp)
                    )

                    val lastComment = post.comments.last()
                    Row(
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastComment.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lastComment.content,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
