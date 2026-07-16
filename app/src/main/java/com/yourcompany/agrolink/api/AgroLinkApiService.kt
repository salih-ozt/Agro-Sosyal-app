package com.yourcompany.agrolink.api

import com.yourcompany.agrolink.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Json
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// API Requests/Responses
data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    val message: String?,
    val token: String?,
    val refreshToken: String?,
    val user: UserResponse?
)

data class UserResponse(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    @Json(name = "profile_pic") val profilePic: String?,
    @Json(name = "cover_pic") val coverPic: String?,
    val bio: String?,
    @Json(name = "is_verified") val isVerified: Boolean?,
    @Json(name = "has_farmer_badge") val hasFarmerBadge: Boolean?,
    val role: String?,
    @Json(name = "is_banned") val isBanned: Boolean?,
    @Json(name = "user_type") val userType: String?,
    val location: String?,
    val website: String?,
    @Json(name = "follower_count") val followerCount: Int?,
    @Json(name = "following_count") val followingCount: Int?,
    @Json(name = "post_count") val postCount: Int?
)

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
    val username: String,
    val userType: String
)

data class RegisterResponse(
    val message: String?,
    val token: String?,
    val refreshToken: String?,
    val user: UserResponse?
)

data class PostCreateRequest(
    val content: String,
    @Json(name = "allow_comments") val allowComments: Boolean = true,
    @Json(name = "location_name") val locationName: String = "",
    @Json(name = "is_poll") val isPoll: Boolean = false,
    @Json(name = "poll_question") val pollQuestion: String = "",
    @Json(name = "poll_options") val pollOptions: List<String> = emptyList(),
    @Json(name = "image_base64") val imageBase64: String? = null,
    @Json(name = "media_urls") val mediaUrls: List<String>? = null
)

data class MessageCreateRequest(
    @Json(name = "recipient_id") val recipientId: String,
    val content: String
)

data class FarmRecordCreateRequest(
    @Json(name = "record_type") val recordType: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "field_name") val fieldName: String,
    @Json(name = "field_size") val fieldSize: Double,
    @Json(name = "field_size_unit") val fieldSizeUnit: String,
    val quantity: Double,
    val unit: String,
    val cost: Double,
    val income: Double,
    @Json(name = "harvest_amount") val harvestAmount: Double,
    @Json(name = "harvest_unit") val harvestUnit: String,
    @Json(name = "quality_rating") val qualityRating: Int,
    @Json(name = "weather_condition") val weatherCondition: String,
    @Json(name = "record_date") val recordDate: String,
    val season: String,
    val year: Int,
    val notes: String
)

data class DiseaseAlarmCreateRequest(
    @Json(name = "crop_type") val cropType: String,
    @Json(name = "disease_info") val diseaseInfo: String,
    val city: String,
    val district: String,
    val note: String
)

data class FieldTrackerCreateRequest(
    val name: String,
    val product: String,
    @Json(name = "alan_donm") val alanDonm: Double,
    @Json(name = "tahmini_hasat") val tahminiHasat: String
)

data class UpdateProfileRequest(
    val name: String,
    val bio: String,
    val location: String,
    val website: String,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "cover_pic") val coverPic: String? = null
)

data class ProductCreateRequest(
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val stock: Double,
    @Json(name = "stock_unit") val stockUnit: String,
    @Json(name = "image_base64") val imageBase64: String,
    val location: String
)

data class UserStatsResponse(
    @Json(name = "follower_count") val followerCount: Int? = null,
    @Json(name = "following_count") val followingCount: Int? = null,
    @Json(name = "post_count") val postCount: Int? = null,
    val followers: Int? = null,
    val following: Int? = null,
    val posts: Int? = null
)

interface AgroLinkApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @GET("/api/me")
    suspend fun getMe(): UserResponse

    @GET("/api/users/{userId}/stats")
    suspend fun getUserStats(
        @Path("userId") userId: String
    ): UserStatsResponse

    @GET("/api/users/me/stats")
    suspend fun getMeStats(): UserStatsResponse

    @GET("/api/feed")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("refresh") refresh: Int? = null
    ): okhttp3.ResponseBody

    @GET("/api/users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): okhttp3.ResponseBody

    @POST("/api/posts")
    suspend fun createPost(@Body body: PostCreateRequest): Post

    @POST("/api/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Any

    @POST("/api/posts/{id}/save")
    suspend fun savePost(@Path("id") postId: String): Any

    @GET("/api/stories")
    suspend fun getStories(): List<Story>

    @GET("/api/messages/conversations")
    suspend fun getConversations(): List<Conversation>

    @GET("/api/messages/{userId}")
    suspend fun getMessages(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<Message>

    @POST("/api/messages")
    suspend fun sendMessage(@Body body: MessageCreateRequest): Message

    @GET("/api/farmbook/records")
    suspend fun getFarmRecords(): List<FarmRecord>

    @POST("/api/farmbook/records")
    suspend fun createFarmRecord(@Body body: FarmRecordCreateRequest): FarmRecord

    @DELETE("/api/farmbook/records/{id}")
    suspend fun deleteFarmRecord(@Path("id") id: String): Any

    @GET("/api/disease-alarms")
    suspend fun getDiseaseAlarms(): List<DiseaseAlarm>

    @POST("/api/disease-alarms")
    suspend fun createDiseaseAlarm(@Body body: DiseaseAlarmCreateRequest): DiseaseAlarm

    @GET("/api/tarim-fiyatlari")
    suspend fun getTarimFiyatlari(): List<CropPrice>

    @GET("/api/store/products")
    suspend fun getProducts(): List<Product>

    @PUT("/api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserResponse

    @Multipart
    @PUT("/api/users/profile")
    suspend fun updateProfileMultipart(
        @Part("name") name: okhttp3.RequestBody,
        @Part("bio") bio: okhttp3.RequestBody,
        @Part("location") location: okhttp3.RequestBody,
        @Part("website") website: okhttp3.RequestBody,
        @Part profilePic: okhttp3.MultipartBody.Part? = null
    ): UserResponse

    @DELETE("/api/users/profile-pic")
    suspend fun deleteProfilePic(
        @Query("type") type: String
    ): okhttp3.ResponseBody

    @POST("/api/store/products")
    suspend fun createProduct(@Body body: ProductCreateRequest): Product

    @POST("/api/device-token")
    suspend fun registerDeviceToken(@Body body: Map<String, String>)

    @HTTP(method = "DELETE", path = "/api/device-token", hasBody = true)
    suspend fun deleteDeviceToken(@Body body: Map<String, String>)

    companion object {
        private var accessToken: String? = null
        private var baseUrl = "https://www.sehitumitkestitarimmtal.com"

        fun setToken(token: String?) {
            accessToken = token
        }

        fun createClient(): AgroLinkApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val httpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("X-App-Platform", "android")
                        .header("X-Client-Type", "native")
                        .header("Content-Type", "application/json")

                    accessToken?.let {
                        requestBuilder.header("Authorization", "Bearer $it")
                    }

                    chain.proceed(requestBuilder.build())
                }
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AgroLinkApiService::class.java)
        }
    }
}
