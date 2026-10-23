package com.example.data.repository

import com.example.R
import com.example.data.model.CommunityPost
import com.example.data.model.MediaType
import com.example.data.model.PostComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CommunityRepository {

    private val initialPosts = mutableListOf(
        CommunityPost(
            id = "post_1",
            authorName = "Hamza Farooq",
            authorEmail = "hamza.engineer@gmail.com",
            authorCity = "DHA Phase 6, Lahore",
            caption = "Successfully energized our 10.4 kW Hybrid Solar setup with Urban Soltech! 18x Longi 585W bifacial panels + 10kW Deye Inverter and 10kWh LiFePO4 battery bank. Air conditioners running smooth off-grid during daytime peak hours. Loving the zero electricity bill!",
            mediaType = MediaType.IMAGE,
            localDrawableRes = R.drawable.solar_hero_banner_1792757120461,
            systemTag = "10 kW Hybrid",
            likesCount = 42,
            isLiked = false,
            comments = mutableListOf(
                PostComment(
                    postId = "post_1",
                    authorName = "Zubair Tariq",
                    authorEmail = "zubair.t@gmail.com",
                    text = "MashAllah looks super clean! How much was your total cost including L3 elevated structure?",
                    timestampMillis = System.currentTimeMillis() - 7200000L
                ),
                PostComment(
                    postId = "post_1",
                    authorName = "Hamza Farooq",
                    authorEmail = "hamza.engineer@gmail.com",
                    text = "Around 1.8M PKR complete turnkey with net metering approval by LESCO. The Urban Soltech engineering crew handled all DISCO paperwork.",
                    timestampMillis = System.currentTimeMillis() - 3600000L
                )
            ),
            timestampMillis = System.currentTimeMillis() - 14400000L
        ),
        CommunityPost(
            id = "post_2",
            authorName = "Engr. Bilal Khan",
            authorEmail = "bilal.soltech@gmail.com",
            authorCity = "F-7/2, Islamabad",
            caption = "Video walkthrough of our newly completed 20 kW On-Grid Net-Metering project for a commercial plaza. Dual MPPT Sungrow Inverter with Canadian Solar TOPBiHiKu7 panels. Exporting over 85 units daily back to IESCO grid!",
            mediaType = MediaType.VIDEO,
            localDrawableRes = R.drawable.solar_hero_banner_1792757120461,
            systemTag = "20 kW On-Grid",
            likesCount = 68,
            isLiked = true,
            comments = mutableListOf(
                PostComment(
                    postId = "post_2",
                    authorName = "Rashid Mahmood",
                    authorEmail = "rmahmood@gmail.com",
                    text = "Brilliant cable management and earthing pit testing. Did you use 6mm or 4mm DC cables?",
                    timestampMillis = System.currentTimeMillis() - 1800000L
                )
            ),
            timestampMillis = System.currentTimeMillis() - 28800000L
        ),
        CommunityPost(
            id = "post_3",
            authorName = "Dr. Asim Siddiqui",
            authorEmail = "drasim@gmail.com",
            authorCity = "Clifton, Karachi",
            caption = "5 kW Off-Grid backup system tested during yesterday's 6-hour power outage. Refrigerator, fans, and 1.5-ton inverter AC ran continuously without a single voltage dip. Outstanding support from Urban Soltech helpline!",
            mediaType = MediaType.IMAGE,
            localDrawableRes = R.drawable.urban_soltech_icon_1792757105787,
            systemTag = "5 kW Off-Grid",
            likesCount = 31,
            isLiked = false,
            comments = mutableListOf(),
            timestampMillis = System.currentTimeMillis() - 50000000L
        )
    )

    private val _postsState = MutableStateFlow<List<CommunityPost>>(initialPosts)
    val postsState: StateFlow<List<CommunityPost>> = _postsState.asStateFlow()

    fun toggleLike(postId: String) {
        val updatedList = _postsState.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
                post.copy(isLiked = newLiked, likesCount = newCount)
            } else {
                post
            }
        }
        _postsState.value = updatedList
    }

    fun addComment(postId: String, authorName: String, authorEmail: String, text: String) {
        if (text.isBlank()) return
        val newComment = PostComment(
            postId = postId,
            authorName = authorName,
            authorEmail = authorEmail,
            text = text.trim()
        )
        val updatedList = _postsState.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.toMutableList()
                updatedComments.add(newComment)
                post.copy(comments = updatedComments)
            } else {
                post
            }
        }
        _postsState.value = updatedList
    }

    fun createPost(
        authorName: String,
        authorEmail: String,
        authorCity: String,
        caption: String,
        mediaType: MediaType,
        mediaUri: String?,
        systemTag: String
    ) {
        val newPost = CommunityPost(
            id = UUID.randomUUID().toString(),
            authorName = authorName.ifBlank { "Urban Solar Member" },
            authorEmail = authorEmail,
            authorCity = authorCity.ifBlank { "Pakistan" },
            caption = caption,
            mediaType = mediaType,
            mediaUri = mediaUri,
            localDrawableRes = if (mediaUri == null) R.drawable.solar_hero_banner_1792757120461 else null,
            systemTag = systemTag.ifBlank { "Rooftop Solar" },
            likesCount = 1,
            isLiked = true,
            comments = mutableListOf(),
            timestampMillis = System.currentTimeMillis()
        )
        val updatedList = listOf(newPost) + _postsState.value
        _postsState.value = updatedList
    }
}
