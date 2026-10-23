package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CommunityPost
import com.example.data.model.MediaType
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.GreenLive
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SolarYellow
import com.example.ui.theme.SolarYellowBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityRepository: CommunityRepository,
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    val posts by communityRepository.postsState.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState()

    var showCreatePostSheet by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<CommunityPost?>(null) }
    var selectedTagFilter by remember { mutableStateOf("All Projects") }

    val tags = listOf("All Projects", "10 kW Hybrid", "20 kW On-Grid", "5 kW Off-Grid", "Net Metering")

    val filteredPosts = if (selectedTagFilter == "All Projects") {
        posts
    } else {
        posts.filter { it.systemTag.contains(selectedTagFilter, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("community_screen"),
        containerColor = SlateBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostSheet = true },
                containerColor = SolarYellow,
                contentColor = TextOnYellow,
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Post")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Solar Project", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Community Feed Header
            item {
                Column {
                    Text(
                        text = "SOLAR COMMUNITY FEED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolarYellow
                    )
                    Text(
                        text = "Share photos, video walkthroughs, and discuss rooftop installations across Pakistan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Tag Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tags) { tag ->
                        val isSelected = selectedTagFilter == tag
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) SolarYellow else SlateSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedTagFilter = tag }
                                .testTag("tag_filter_$tag")
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextOnYellow else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Posts Feed
            items(filteredPosts, key = { it.id }) { post ->
                CommunityPostCard(
                    post = post,
                    onToggleLike = { communityRepository.toggleLike(post.id) },
                    onOpenComments = { selectedPostForComments = post }
                )
            }
        }

        // Create Post Sheet
        if (showCreatePostSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreatePostSheet = false },
                containerColor = SlateSurface
            ) {
                CreatePostBottomSheetContent(
                    currentUserEmail = currentUser?.email ?: "user@gmail.com",
                    currentUserName = currentUser?.fullName ?: "Solar Enthusiast",
                    onDismiss = { showCreatePostSheet = false },
                    onSubmit = { caption, mediaType, mediaUri, systemTag ->
                        communityRepository.createPost(
                            authorName = currentUser?.fullName ?: "Solar Enthusiast",
                            authorEmail = currentUser?.email ?: "user@gmail.com",
                            authorCity = currentUser?.city ?: "Lahore, Pakistan",
                            caption = caption,
                            mediaType = mediaType,
                            mediaUri = mediaUri,
                            systemTag = systemTag
                        )
                        showCreatePostSheet = false
                        Toast.makeText(context, "Project published to community!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Comments Bottom Sheet
        selectedPostForComments?.let { post ->
            // Re-find the post from state so newly added comments appear immediately
            val latestPost = posts.find { it.id == post.id } ?: post
            ModalBottomSheet(
                onDismissRequest = { selectedPostForComments = null },
                containerColor = SlateSurface
            ) {
                CommentsBottomSheetContent(
                    post = latestPost,
                    currentUserName = currentUser?.fullName ?: "Solar User",
                    currentUserEmail = currentUser?.email ?: "user@gmail.com",
                    onAddComment = { commentText ->
                        communityRepository.addComment(
                            postId = latestPost.id,
                            authorName = currentUser?.fullName ?: "Solar User",
                            authorEmail = currentUser?.email ?: "user@gmail.com",
                            text = commentText
                        )
                    },
                    onClose = { selectedPostForComments = null }
                )
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit
) {
    val context = LocalContext.current
    var isVideoPlaying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(SolarYellow, SolarAmber))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Member",
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${post.authorCity} • ${formatRelativeTime(post.timestampMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSurfaceVariant
                ) {
                    Text(
                        text = post.systemTag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SolarYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Media Container (Photo or Video Player Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                if (post.mediaUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(post.mediaUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (post.localDrawableRes != null) {
                    Image(
                        painter = painterResource(id = post.localDrawableRes),
                        contentDescription = "Post Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // If Video: Show Interactive Video Overlay & Player Controls
                if (post.mediaType == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isVideoPlaying) Color.Transparent else Color.Black.copy(alpha = 0.35f))
                            .clickable { isVideoPlaying = !isVideoPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isVideoPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SolarYellow.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Video Badge / Watermark
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = SolarYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isVideoPlaying) "PLAYING" else "0:45 HD",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Social Action Buttons: Likes, Comments, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like Button
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier.testTag("like_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) EmergencyRed else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isLiked) EmergencyRed else TextSecondary
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    // Comments Button
                    IconButton(
                        onClick = onOpenComments,
                        modifier = Modifier.testTag("comments_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.comments.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${post.comments.size} Comments",
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarYellow,
                    modifier = Modifier.clickable { onOpenComments() }
                )
            }
        }
    }
}

@Composable
fun CreatePostBottomSheetContent(
    currentUserEmail: String,
    currentUserName: String,
    onDismiss: () -> Unit,
    onSubmit: (caption: String, mediaType: MediaType, mediaUri: String?, systemTag: String) -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf(MediaType.IMAGE) }
    var systemTagText by remember { mutableStateOf("10 kW Hybrid") }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("create_post_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Share Solar Installation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SolarYellow
            )
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Media Type Selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { selectedMediaType = MediaType.IMAGE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMediaType == MediaType.IMAGE) SolarYellow else SlateSurfaceVariant,
                    contentColor = if (selectedMediaType == MediaType.IMAGE) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Photo Post")
            }

            Button(
                onClick = { selectedMediaType = MediaType.VIDEO },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMediaType == MediaType.VIDEO) SolarYellow else SlateSurfaceVariant,
                    contentColor = if (selectedMediaType == MediaType.VIDEO) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Video Clip")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Photo / Video picker trigger
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SlateSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val request = if (selectedMediaType == MediaType.IMAGE) {
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    } else {
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    }
                    mediaPickerLauncher.launch(request)
                }
                .testTag("media_picker_button")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedMediaType == MediaType.IMAGE) Icons.Default.Image else Icons.Default.Videocam,
                    contentDescription = null,
                    tint = SolarYellow
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (selectedMediaUri != null) "Media Attached ✓" else "Select ${if (selectedMediaType == MediaType.IMAGE) "Photo" else "Video"} from Gallery",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (selectedMediaUri != null) "Ready to upload" else "Rooftop inverter, panels or battery setup",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = systemTagText,
            onValueChange = { systemTagText = it },
            label = { Text("System Tag (e.g. 10 kW Hybrid - Lahore)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SolarYellow,
                unfocusedBorderColor = SlateBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = captionText,
            onValueChange = { captionText = it },
            label = { Text("Share your installation details, units generated, and experience...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("post_caption_input"),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SolarYellow,
                unfocusedBorderColor = SlateBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (captionText.isNotBlank()) {
                    onSubmit(captionText.trim(), selectedMediaType, selectedMediaUri, systemTagText.trim())
                }
            },
            enabled = captionText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = TextOnYellow),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("publish_post_btn")
        ) {
            Text("Publish to Urban Soltech Community", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CommentsBottomSheetContent(
    post: CommunityPost,
    currentUserName: String,
    currentUserEmail: String,
    onAddComment: (String) -> Unit,
    onClose: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("comments_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Community Comments (${post.comments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SolarYellow
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (post.comments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No comments yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Be the first to share your thoughts or ask questions!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(post.comments, key = { it.id }) { comment ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SlateSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SolarYellow
                                )
                                Text(
                                    text = formatRelativeTime(comment.timestampMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Comment Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                placeholder = { Text("Write a comment...", color = TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("comment_input_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolarYellow,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (newCommentText.isNotBlank()) {
                        onAddComment(newCommentText.trim())
                        newCommentText = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SolarYellow)
                    .testTag("send_comment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Comment",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

private fun formatRelativeTime(timeMillis: Long): String {
    val diff = System.currentTimeMillis() - timeMillis
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.US)
            sdf.format(Date(timeMillis))
        }
    }
}
