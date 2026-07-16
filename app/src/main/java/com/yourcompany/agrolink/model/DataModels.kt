package com.yourcompany.agrolink.model

import java.util.UUID
import com.squareup.moshi.Json

// User Types in AgroLink: Farmer, Agricultural Engineer, Supplier, Academician, Consumer
enum class UserType(val label: String) {
    FARMER("Çiftçi"),
    ENGINEER("Ziraat Mühendisi"),
    SUPPLIER("Tedarikçi"),
    ACADEMIC("Akademisyen"),
    CONSUMER("Tüketici")
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val name: String,
    val email: String,
    val profilePic: String = "", // Holds base64 or placeholder URLs
    val coverPic: String = "",
    val bio: String = "",
    val isVerified: Boolean = false, // Blue tick
    val hasFarmerBadge: Boolean = false, // Farmer golden badge
    val role: String = "user",
    val isBanned: Boolean = false,
    val userType: UserType = UserType.FARMER,
    val location: String = "",
    val website: String = "",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0
)

data class Post(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "user_id") val userId: String,
    @Json(name = "author_username") val authorUsername: String,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "author_profile_pic") val authorProfilePic: String = "",
    @Json(name = "author_user_type") val authorUserType: UserType = UserType.FARMER,
    @Json(name = "has_verified_badge") val hasVerifiedBadge: Boolean = false,
    val content: String,
    @Json(name = "media_urls") val mediaUrls: List<String> = emptyList(),
    @Json(name = "location_name") val locationName: String = "",
    @Json(name = "like_count") val likeCount: Int = 0,
    @Json(name = "comment_count") val commentCount: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    @Json(name = "is_saved") val isSaved: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "allow_comments") val allowComments: Boolean = true,
    @Json(name = "is_poll") val isPoll: Boolean = false,
    @Json(name = "poll_question") val pollQuestion: String = "",
    @Json(name = "poll_options") val pollOptions: List<PollOption> = emptyList(),
    val comments: List<Comment> = emptyList()
)

data class PollOption(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val votes: Int = 0,
    @Json(name = "is_voted") val isVoted: Boolean = false
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String,
    val username: String,
    @Json(name = "profile_pic") val profilePic: String = "",
    val content: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "like_count") val likeCount: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

data class Story(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "user_id") val userId: String,
    val username: String,
    @Json(name = "profile_pic") val profilePic: String = "",
    @Json(name = "media_url") val mediaUrl: String = "", // Base64 image
    val text: String = "",
    @Json(name = "text_color") val textColor: String = "#FFFFFF",
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    @Json(name = "viewers_count") val viewersCount: Int = 0
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "partner_username") val partnerUsername: String,
    @Json(name = "partner_name") val partnerName: String,
    @Json(name = "partner_profile_pic") val partnerProfilePic: String = "",
    @Json(name = "last_message") val lastMessage: String = "",
    @Json(name = "last_message_time") val lastMessageTime: String = "",
    @Json(name = "unread_count") val unreadCount: Int = 0,
    @Json(name = "is_online") val isOnline: Boolean = false
)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "sender_id") val senderId: String,
    val content: String,
    val timestamp: String,
    @Json(name = "is_image") val isImage: Boolean = false,
    @Json(name = "is_voice") val isVoice: Boolean = false,
    @Json(name = "media_url") val mediaUrl: String = ""
)

// Farmbook (Çiftlik Defteri) Record Model
data class FarmRecord(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "record_type") val recordType: String, // Gelir, Gider, Hasat, İlaçlama, Gübreleme, Sulama
    @Json(name = "product_name") val productName: String,
    @Json(name = "field_name") val fieldName: String,
    @Json(name = "field_size") val fieldSize: Double = 0.0,
    @Json(name = "field_size_unit") val fieldSizeUnit: String = "Dönüm",
    val quantity: Double = 0.0,
    val unit: String = "Kg",
    val cost: Double = 0.0,
    val income: Double = 0.0,
    @Json(name = "harvest_amount") val harvestAmount: Double = 0.0,
    @Json(name = "harvest_unit") val harvestUnit: String = "Ton",
    @Json(name = "quality_rating") val qualityRating: Int = 5,
    @Json(name = "weather_condition") val weatherCondition: String = "Güneşli",
    @Json(name = "record_date") val recordDate: String,
    val season: String,
    val year: Int,
    val notes: String = ""
)

// Hasat Takip (Harvest Tracker) Tarla Model
data class FieldTracker(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val product: String,
    @Json(name = "alan_donm") val alanDonm: Double,
    @Json(name = "tahmini_hasat") val tahminiHasat: String, // Date string
    @Json(name = "photo_base64") val photoBase64: String = "",
    val notes: List<String> = emptyList()
)

// Hastalık Alarmı (Disease Alarm) Model
data class DiseaseAlarm(
    val id: String = UUID.randomUUID().toString(),
    @Json(name = "crop_type") val cropType: String,
    @Json(name = "disease_info") val diseaseInfo: String,
    val city: String,
    val district: String,
    val note: String,
    @Json(name = "reported_by") val reportedBy: String,
    @Json(name = "report_date") val reportDate: String,
    val urgency: String = "Yüksek" // Yüksek, Orta, Düşük
)

// Tarım Fiyatları (Market Prices) Model
data class CropPrice(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    @Json(name = "price_range") val priceRange: String,
    @Json(name = "change_rate") val changeRate: Double, // positive or negative percentage
    val unit: String = "Kg",
    val market: String = "Antalya Hal Fiyatı",
    @Json(name = "is_followed") val isFollowed: Boolean = false
)

// Marketplace Product Model
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // Tohum, Gübre, İlaç, Makine, Mahsul
    val stock: Double,
    @Json(name = "stock_unit") val stockUnit: String = "Adet",
    @Json(name = "seller_name") val sellerName: String,
    @Json(name = "seller_phone") val sellerPhone: String = "",
    @Json(name = "seller_id") val sellerId: String,
    @Json(name = "image_base64") val imageBase64: String = "",
    val location: String = "Konya"
)

// Weather data for farmers
data class FarmerWeather(
    val city: String,
    val temperature: Double,
    val condition: String,
    val icon: String, // sun, rain, cloud etc.
    val humidity: Int,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "air_quality") val airQuality: String = "İyi",
    val recommendation: String = "Bugün ilaçlama ve gübreleme için ideal hava koşulları mevcuttur."
)
