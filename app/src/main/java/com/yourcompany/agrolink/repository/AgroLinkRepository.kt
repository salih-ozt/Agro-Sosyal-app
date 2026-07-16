package com.yourcompany.agrolink.repository

import com.yourcompany.agrolink.model.*
import com.yourcompany.agrolink.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object AgroLinkRepository {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val postListType = Types.newParameterizedType(List::class.java, Post::class.java)
    private val postListAdapter = moshi.adapter<List<Post>>(postListType)

    fun resolveUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:")) {
            return url
        }
        val cleanUrl = if (url.startsWith("/")) url else "/$url"
        return "https://www.sehitumitkestitarimmtal.com$cleanUrl"
    }

    private fun parsePostsResponse(json: String): List<Post> {
        return try {
            val rawList = if (json.trim().startsWith("[")) {
                postListAdapter.fromJson(json) ?: emptyList()
            } else {
                val jsonObject = JSONObject(json)
                val arrayStr = when {
                    jsonObject.has("posts") -> jsonObject.getJSONArray("posts").toString()
                    jsonObject.has("data") -> jsonObject.getJSONArray("data").toString()
                    else -> "[]"
                }
                postListAdapter.fromJson(arrayStr) ?: emptyList()
            }
            rawList.map { post ->
                post.copy(
                    authorProfilePic = if (post.authorProfilePic.isNotBlank()) resolveUrl(post.authorProfilePic) else "",
                    mediaUrls = post.mediaUrls.map { resolveUrl(it) }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Current logged-in user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Posts state
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // My Posts state
    private val _myPosts = MutableStateFlow<List<Post>>(emptyList())
    val myPosts: StateFlow<List<Post>> = _myPosts.asStateFlow()

    // Stories state
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    // Conversations state
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Messages state (grouped by conversationId)
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Farmbook Records
    private val _farmRecords = MutableStateFlow<List<FarmRecord>>(emptyList())
    val farmRecords: StateFlow<List<FarmRecord>> = _farmRecords.asStateFlow()

    // Hasat Tarlalar Tracker
    private val _fieldTrackers = MutableStateFlow<List<FieldTracker>>(emptyList())
    val fieldTrackers: StateFlow<List<FieldTracker>> = _fieldTrackers.asStateFlow()

    // Disease Alarms
    private val _diseaseAlarms = MutableStateFlow<List<DiseaseAlarm>>(emptyList())
    val diseaseAlarms: StateFlow<List<DiseaseAlarm>> = _diseaseAlarms.asStateFlow()

    // Tarım Fiyatları (Market Prices)
    private val _cropPrices = MutableStateFlow<List<CropPrice>>(emptyList())
    val cropPrices: StateFlow<List<CropPrice>> = _cropPrices.asStateFlow()

    // Marketplace Products
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Weather state
    private val _weather = MutableStateFlow<FarmerWeather?>(null)
    val weather: StateFlow<FarmerWeather?> = _weather.asStateFlow()

    // Pagination & load-more states
    private val _hasMorePosts = MutableStateFlow(true)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _isFeedLoading = MutableStateFlow(false)
    val isFeedLoading: StateFlow<Boolean> = _isFeedLoading.asStateFlow()

    // SharedPreferences for offline caching and token management
    private var prefs: android.content.SharedPreferences? = null
    private var apiService = AgroLinkApiService.createClient()

    init {
        // Mock data removed. App operates strictly on Prod data.
    }

    fun initialize(context: android.content.Context) {
        prefs = context.getSharedPreferences("agrolink_prefs", android.content.Context.MODE_PRIVATE)
        val token = prefs?.getString("access_token", null)
        val storedUserId = prefs?.getString("user_id", null)
        val storedUsername = prefs?.getString("user_username", null)
        val storedName = prefs?.getString("user_name", null)
        val storedEmail = prefs?.getString("user_email", null)
        val storedTypeStr = prefs?.getString("user_type", null)
        val storedProfilePic = prefs?.getString("user_profile_pic", "") ?: ""
        val storedCoverPic = prefs?.getString("user_cover_pic", "") ?: ""

        // Automatic persistence flow
        GlobalScope.launch {
            _currentUser.collect { user ->
                if (user != null) {
                    prefs?.edit()?.apply {
                        putString("user_id", user.id)
                        putString("user_username", user.username)
                        putString("user_name", user.name)
                        putString("user_email", user.email)
                        putString("user_type", user.userType.name)
                        putString("user_profile_pic", user.profilePic)
                        putString("user_cover_pic", user.coverPic)
                        putBoolean("user_verified", user.isVerified)
                        putBoolean("user_farmer_badge", user.hasFarmerBadge)
                        apply()
                    }
                }
            }
        }

        if (token != null && storedUserId != null && storedUsername != null && storedName != null && storedEmail != null) {
            AgroLinkApiService.setToken(token)
            apiService = AgroLinkApiService.createClient()
            val userType = try {
                UserType.valueOf(storedTypeStr ?: "FARMER")
            } catch (e: Exception) {
                UserType.FARMER
            }
            _currentUser.value = User(
                id = storedUserId,
                username = storedUsername,
                name = storedName,
                email = storedEmail,
                userType = userType,
                profilePic = storedProfilePic,
                coverPic = storedCoverPic,
                isVerified = prefs?.getBoolean("user_verified", false) ?: false,
                hasFarmerBadge = prefs?.getBoolean("user_farmer_badge", false) ?: false
            )
            // Fetch live system updates
            fetchLiveData()
        } else {
            // Wait for user to explicitly log in
            _currentUser.value = null
        }
    }

    fun resetToDefaultMockData() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val nowStr = dateFormat.format(Date())

        // Initial default seed fallback user
        _currentUser.value = User(
            id = "me_user_123",
            username = "ahmet_bostanci",
            name = "Ahmet Bostancı",
            email = "ahmet.farmer@agrolink.com",
            profilePic = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            coverPic = "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800",
            bio = "🌾 Konya Karatay'da buğday, arpa ve mısır üreticisi.\n🚜 Tarımda teknolojiyi ve modern teknikleri seviyorum.\n📍 Konya, Türkiye",
            isVerified = true,
            hasFarmerBadge = true,
            location = "Konya, Karatay",
            website = "www.bostancitarim.com",
            followerCount = 1240,
            followingCount = 482,
            postCount = 12
        )

        val user1 = User("u1", "ziraat_müh_merve", "Merve Yılmaz", "merve@agrolink.com", isVerified = true, userType = UserType.ENGINEER)
        val user2 = User("u2", "akademik_prof_selim", "Prof. Dr. Selim Kaya", "selim@agrolink.com", isVerified = true, userType = UserType.ACADEMIC)
        val user3 = User("u3", "ekici_gubre", "Ekici Gübre ve Tohum", "ekici@agrolink.com", isVerified = false, userType = UserType.SUPPLIER)
        val user4 = User("u4", "genc_ciftci_yusuf", "Yusuf Demir", "yusuf@agrolink.com", isVerified = false, userType = UserType.FARMER)

        _stories.value = listOf(
            Story("s1", user1.id, user1.username, user1.profilePic, "https://images.unsplash.com/photo-1592417817098-8f3d6eb19675?w=500", "Yeni gübreleme rehberi hikayemde!", "#000000", nowStr, viewersCount = 182),
            Story("s2", user4.id, user4.username, user4.profilePic, "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=500", "Sabah hasadı başladı 🚜🌾", "#FFFFFF", nowStr, viewersCount = 94),
            Story("s3", user2.id, user2.username, user2.profilePic, "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=500", "Toprak analizinin önemi 🧪", "#E0F2FE", nowStr, viewersCount = 312),
            Story("s4", user3.id, user3.username, user3.profilePic, "https://images.unsplash.com/photo-1628352081506-83c43123ed6d?w=500", "Tohumlarda %20 indirim başladı! 🏷️", "#FEE2E2", nowStr, viewersCount = 78)
        )

        _posts.value = listOf(
            Post(
                id = "p1",
                userId = user1.id,
                authorUsername = user1.username,
                authorName = user1.name,
                authorProfilePic = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                authorUserType = user1.userType,
                hasVerifiedBadge = true,
                content = "🌾 Yaprak biti zararlısına karşı mücadelede biyolojik yöntemler her zaman ilk tercihiniz olmalı. Uğur böcekleri (gelin böcekleri) yaprak bitlerinin en büyük doğal düşmanıdır! Kimyasal ilaçlamadan önce tarlanızdaki yararlı böcek popülasyonunu mutlaka kontrol edin. #biyolojikmücadele #tarım #ziraat #agrolink",
                mediaUrls = listOf("https://images.unsplash.com/photo-1560493676-04071c5f467b?w=600"),
                locationName = "Adana Çukurova",
                likeCount = 342,
                commentCount = 28,
                isLiked = false,
                isSaved = true,
                createdAt = "2 saat önce",
                comments = listOf(
                    Comment(UUID.randomUUID().toString(), "p1", user4.id, user4.username, "", "Çok yararlı bir bilgi Merve Hanım, teşekkürler! Çukurova'da durumlar nasıl?", "1 saat önce", 12),
                    Comment(UUID.randomUUID().toString(), "p1", "me_user_123", "ahmet_bostanci", "", "Bizim Karatay'da da mısırlarda yaprak biti göründü, hemen deneyeceğim.", "45 dk önce", 4)
                )
            ),
            Post(
                id = "p2",
                userId = user4.id,
                authorUsername = user4.username,
                authorName = user4.name,
                authorProfilePic = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                authorUserType = user4.userType,
                hasVerifiedBadge = false,
                content = "Sizce bu sezon mısırda damlama sulama mı yoksa yağmurlama mı daha karlı ve verimli olur? Kararsız kaldım, tecrübeli çiftçi dostlarımın ve mühendislerimizin fikrini merak ediyorum. Anketi oylayabilirsiniz 👇 #mısır #sulama #çiftçilik",
                locationName = "Konya Ereğli",
                likeCount = 89,
                commentCount = 14,
                createdAt = "5 saat önce",
                isPoll = true,
                pollQuestion = "Mısır sulamasında hangisi?",
                pollOptions = listOf(
                    PollOption("op1", "Damlama Sulama (Yüksek Verim)", 142, false),
                    PollOption("op2", "Yağmurlama (Düşük Yatırım)", 58, false)
                ),
                comments = listOf(
                    Comment(UUID.randomUUID().toString(), "p2", user2.id, user2.username, "", "Kesinlikle damlama sulama. Su tasarrufu ve gübrenin direkt köke ulaşması açısından mısırda fark yaratır.", "3 saat önce", 21)
                )
            ),
            Post(
                id = "p3",
                userId = user2.id,
                authorUsername = user2.username,
                authorName = user2.name,
                authorProfilePic = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                authorUserType = user2.userType,
                hasVerifiedBadge = true,
                content = "🔬 Toprağın organik madde miktarını artırmanın yolları: 1- Ahır gübresi kullanımı, 2- Yeşil gübreleme (Baklagil ekimi ve toprağa karıştırılması), 3- Anız yakılmasının kesinlikle önlenmesi! Topraklarımızı çölleşmekten korumalıyız. #toprakanalizi #organikmadde #akademik #sürdürülebilirtarım",
                mediaUrls = listOf("https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=600"),
                locationName = "Ege Üniversitesi Ziraat Fakültesi",
                likeCount = 512,
                commentCount = 42,
                isLiked = true,
                createdAt = "1 gün önce"
            )
        )

        _conversations.value = listOf(
            Conversation("c1", user1.id, user1.username, user1.name, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", "Uğur böcekleri tarlayı kurtardı mı?", "10:15", 1, true),
            Conversation("c2", user4.id, user4.username, user4.name, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", "Abi sulama borusu fiyatları ne durumda?", "Dün", 0, false),
            Conversation("c3", user2.id, user2.username, user2.name, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", "Analiz sonucunu mail attım Selim Hocam.", "Salı", 0, false)
        )

        _messages.value = mapOf(
            "c1" to listOf(
                Message("m1", "c1", "me_user_123", "Merve Hanım selamlar, domateslerde yaprak kıvrılması var, ne yapmalıyım?", "10:00"),
                Message("m2", "c1", user1.id, "Selam Ahmet Bey, muhtemelen kırmızı örümcek veya thrips zararlısıdır. Yaprak arkasını incelediniz mi?", "10:05"),
                Message("m3", "c1", "me_user_123", "Evet, küçük kırmızı noktalar geziyor arkasında.", "10:12"),
                Message("m4", "c1", user1.id, "Kırmızı örümcek kesinleşti. Akarisit grubu bir ilaçlama yapabilirsiniz veya faydalı akarlardan yararlanabilirsiniz.", "10:14"),
                Message("m5", "c1", user1.id, "Uğur böcekleri tarlayı kurtardı mı?", "10:15")
            )
        )

        _farmRecords.value = listOf(
            FarmRecord(UUID.randomUUID().toString(), "Gider", "DAP Gübresi", "Karatay Merkez Tarla", 40.0, "Dönüm", 1200.0, "Kg", 24000.0, 0.0, 0.0, "Kg", 5, "Bulutlu", "12.04.2026", "İlkbahar", 2026, "Tarlaya taban gübresi atıldı."),
            FarmRecord(UUID.randomUUID().toString(), "Gider", "Mısır Tohumu (Pioneer)", "Kuyulu Tarla", 25.0, "Dönüm", 10.0, "Torba", 18500.0, 0.0, 0.0, "Kg", 5, "Güneşli", "05.05.2026", "İlkbahar", 2026, "Tohum ekimi yapıldı."),
            FarmRecord(UUID.randomUUID().toString(), "Gelir", "Arpa Hasat Satışı", "Yayla Tarlası", 50.0, "Dönüm", 22.5, "Ton", 0.0, 191250.0, 22.5, "Ton", 4, "Güneşli", "30.06.2026", "Yaz", 2026, "TMO Ofisine arpa teslim edildi. Kg fiyatı 8.5 TL."),
            FarmRecord(UUID.randomUUID().toString(), "Gider", "Traktör Mazot Alımı", "Tüm Alanlar", 115.0, "Dönüm", 350.0, "Litre", 14800.0, 0.0, 0.0, "Litre", 5, "Yağmurlu", "10.07.2026", "Yaz", 2026, "Hasat öncesi mazot takviyesi yapıldı.")
        )

        _fieldTrackers.value = listOf(
            FieldTracker(UUID.randomUUID().toString(), "Karatay Merkez", "Mısır", 40.0, "15.09.2026", "", listOf("05.05.2026 tarihinde ekildi.", "Gübreleme tamamlandı. Sulama haftalık yapılıyor.")),
            FieldTracker(UUID.randomUUID().toString(), "Kuyulu Tarla", "Sarı Buğday", 35.0, "20.07.2026", "", listOf("Hasat olgunluğuna erişti, biçerdöver sırası bekleniyor.", "Dane dolumu mükemmel durumda.")),
            FieldTracker(UUID.randomUUID().toString(), "Yayla Tarlası", "Arpa", 50.0, "30.06.2026", "", listOf("Hasat tamamlandı, tarla nadasa bırakılacak."))
        )

        _diseaseAlarms.value = listOf(
            DiseaseAlarm(UUID.randomUUID().toString(), "Mısır", "Mısır Rastığı (Ustilago maydis)", "Konya", "Karatay", "Mısır koçanlarında siyah şişkinlikler ve mantarsı yapılar görüldü. Hemen ilaçlama yapılmalı.", "Zir. Müh. Cemal Bal", "14.07.2026", "Yüksek"),
            DiseaseAlarm(UUID.randomUUID().toString(), "Buğday", "Sarı Pas Hastalığı (Puccinia striiformis)", "Ankara", "Polatlı", "Yapraklarda sarı, püstül şeklinde çizgiler oluşuyor. Rüzgarla hızla yayılıyor. Acil önlem!", "Prof. Dr. Selim Kaya", "10.07.2026", "Yüksek"),
            DiseaseAlarm(UUID.randomUUID().toString(), "Domates", "Erken Yaprak Yanıklığı (Alternaria solani)", "Antalya", "Aksu", "Alt yapraklarda halkalı kahverengi lekeler başladı. Nemli hava yayılımı tetikliyor.", "Merve Yılmaz", "15.07.2026", "Orta")
        )

        _cropPrices.value = listOf(
            CropPrice(UUID.randomUUID().toString(), "Ekmeklik Buğday", "8.20 - 8.90 TL", 1.8, "Kg", "Konya Ticaret Borsası", true),
            CropPrice(UUID.randomUUID().toString(), "Mısır (Dane)", "7.10 - 7.60 TL", -0.5, "Kg", "Adana Ticaret Borsası", true),
            CropPrice(UUID.randomUUID().toString(), "Arpa", "7.80 - 8.30 TL", 2.2, "Kg", "Polatlı Ticaret Borsası", false),
            CropPrice(UUID.randomUUID().toString(), "Kırmızı Mercimek", "24.50 - 27.00 TL", 0.0, "Kg", "Gaziantep Ticaret Borsası", false),
            CropPrice(UUID.randomUUID().toString(), "Pamuk (Kütlü)", "22.00 - 23.50 TL", -1.2, "Kg", "İzmir Ticaret Borsası", false)
        )

        _products.value = listOf(
            Product(UUID.randomUUID().toString(), "Pioneer Mısır Tohumu 31Y43", "Yüksek verimli, kuraklığa dayanıklı dane mısır tohumu. Orijinal çuvalında faturasıyla teslim.", 3450.0, "Tohum", 15.0, "Torba", "Ekici Gübre ve Tohum", "0532 111 2233", "u3", "", "Konya"),
            Product(UUID.randomUUID().toString(), "Sıvı Organik Gübre - AgroHumus", "Hümik fulvik asit oranı yüksek, toprak yapısını düzenleyen 20L sıvı gübre. Her ürüne uygun.", 1250.0, "Gübre", 50.0, "Bidon", "Gübre Market Ltd.", "0212 555 4433", "u5", "", "İzmir"),
            Product(UUID.randomUUID().toString(), "Massey Ferguson Traktör Lastiği", "Arka lastik, az kullanılmış, yırtık patlak yoktur. %85 diş derinliği.", 8500.0, "Makine", 2.0, "Adet", "Yusuf Demir", "0543 222 5566", "u4", "", "Konya"),
            Product(UUID.randomUUID().toString(), "Sıfır İlaçlama Pompası (Holder)", "600 Litre kapasiteli, turbo fanlı, çekilir tip bahçe ilaçlama makinesi. Kullanılmadı sıfır.", 42000.0, "Makine", 1.0, "Adet", "Asil Tarım Makinaları", "0224 888 9900", "u6", "", "Bursa")
        )

        _weather.value = FarmerWeather(
            city = "Konya",
            temperature = 29.5,
            condition = "Güneşli",
            icon = "sun",
            humidity = 32,
            windSpeed = 12.5,
            airQuality = "Mükemmel",
            recommendation = "🌿 Bugün Karatay bölgesinde buğday hasadı için hava çok elverişli. Nem oranı düşük, rüzgar biçerdöver çalışması için güvenli sınırlarda. İlaçlama yapacak üreticilerimiz sabah veya akşam saatlerini seçebilir."
        )
    }

    suspend fun loadFeed(refresh: Boolean = false) {
        if (_isFeedLoading.value) return
        if (!refresh && !_hasMorePosts.value) return

        _isFeedLoading.value = true
        try {
            val refreshParam = if (refresh) 1 else null
            val response = apiService.getPosts(limit = 20, refresh = refreshParam)
            val jsonStr = response.string()
            
            var fetchedHasMore = true
            try {
                if (!jsonStr.trim().startsWith("[")) {
                    val jsonObject = JSONObject(jsonStr)
                    if (jsonObject.has("hasMore")) {
                        fetchedHasMore = jsonObject.getBoolean("hasMore")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fetchedPosts = parsePostsResponse(jsonStr)
            
            if (refresh) {
                _posts.value = fetchedPosts
                _hasMorePosts.value = fetchedHasMore
            } else {
                if (fetchedPosts.isNotEmpty()) {
                    _posts.value = _posts.value + fetchedPosts
                }
                _hasMorePosts.value = fetchedHasMore && fetchedPosts.isNotEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isFeedLoading.value = false
        }
    }

    fun fetchLiveData() {
        GlobalScope.launch {
            try {
                val me = apiService.getMe()
                val uType = try { UserType.valueOf(me.userType ?: "FARMER") } catch (e: Exception) { UserType.FARMER }
                val current = _currentUser.value
                if (current != null) {
                    _currentUser.value = current.copy(
                        profilePic = if (!me.profilePic.isNullOrBlank()) {
                            resolveUrl(me.profilePic)
                        } else if (current.profilePic.isNotBlank() && !current.profilePic.contains("unsplash.com")) {
                            current.profilePic
                        } else {
                            ""
                        },
                        coverPic = if (!me.coverPic.isNullOrBlank()) {
                            resolveUrl(me.coverPic)
                        } else if (current.coverPic.isNotBlank() && !current.coverPic.contains("unsplash.com")) {
                            current.coverPic
                        } else {
                            ""
                        },
                        bio = me.bio ?: current.bio,
                        location = me.location ?: current.location,
                        website = me.website ?: current.website,
                        followerCount = me.followerCount ?: current.followerCount,
                        followingCount = me.followingCount ?: current.followingCount,
                        postCount = me.postCount ?: current.postCount,
                        userType = uType,
                        isVerified = me.isVerified ?: current.isVerified,
                        hasFarmerBadge = me.hasFarmerBadge ?: current.hasFarmerBadge
                    )
                } else {
                    _currentUser.value = User(
                        id = me.id,
                        username = me.username,
                        name = me.name,
                        email = me.email,
                        profilePic = if (!me.profilePic.isNullOrBlank()) resolveUrl(me.profilePic) else "",
                        coverPic = if (!me.coverPic.isNullOrBlank()) resolveUrl(me.coverPic) else "",
                        bio = me.bio ?: "",
                        isVerified = me.isVerified ?: false,
                        hasFarmerBadge = me.hasFarmerBadge ?: false,
                        userType = uType,
                        location = me.location ?: "Türkiye",
                        website = me.website ?: "",
                        followerCount = me.followerCount ?: 0,
                        followingCount = me.followingCount ?: 0,
                        postCount = me.postCount ?: 0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val stats = apiService.getMeStats()
                val current = _currentUser.value
                if (current != null) {
                    _currentUser.value = current.copy(
                        followerCount = stats.followerCount ?: stats.followers ?: current.followerCount,
                        followingCount = stats.followingCount ?: stats.following ?: current.followingCount,
                        postCount = stats.postCount ?: stats.posts ?: current.postCount
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                loadFeed(refresh = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val current = _currentUser.value
                if (current != null) {
                    val response = apiService.getUserPosts(current.id, 1, 20)
                    val jsonStr = response.string()
                    val fetchedMyPosts = parsePostsResponse(jsonStr)
                    if (fetchedMyPosts.isNotEmpty()) {
                        _myPosts.value = fetchedMyPosts
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedStories = apiService.getStories()
                if (fetchedStories.isNotEmpty()) {
                    _stories.value = fetchedStories
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedConversations = apiService.getConversations()
                if (fetchedConversations.isNotEmpty()) {
                    _conversations.value = fetchedConversations
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedRecords = apiService.getFarmRecords()
                if (fetchedRecords.isNotEmpty()) {
                    _farmRecords.value = fetchedRecords
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedAlarms = apiService.getDiseaseAlarms()
                if (fetchedAlarms.isNotEmpty()) {
                    _diseaseAlarms.value = fetchedAlarms
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedPrices = apiService.getTarimFiyatlari()
                if (fetchedPrices.isNotEmpty()) {
                    _cropPrices.value = fetchedPrices
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val fetchedProducts = apiService.getProducts()
                if (fetchedProducts.isNotEmpty()) {
                    _products.value = fetchedProducts
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // AUTH METHODS
    suspend fun login(identifier: String, password: String): String? {
        return try {
            val response = apiService.login(LoginRequest(identifier, password))
            val token = response.token ?: return "Giriş başarısız: Token sunucudan alınamadı."
            AgroLinkApiService.setToken(token)
            apiService = AgroLinkApiService.createClient()

            val rUser = response.user ?: return "Kullanıcı detayları eksik."
            val uType = try {
                UserType.valueOf(rUser.userType ?: "FARMER")
            } catch (e: Exception) {
                UserType.FARMER
            }

            val loggedInUser = User(
                id = rUser.id,
                username = rUser.username,
                name = rUser.name,
                email = rUser.email,
                profilePic = if (!rUser.profilePic.isNullOrBlank()) resolveUrl(rUser.profilePic) else "",
                coverPic = if (!rUser.coverPic.isNullOrBlank()) resolveUrl(rUser.coverPic) else "",
                bio = rUser.bio ?: "AgroLink Platformu Üreticisi",
                isVerified = rUser.isVerified ?: false,
                hasFarmerBadge = rUser.hasFarmerBadge ?: false,
                userType = uType,
                location = rUser.location ?: "Türkiye",
                website = rUser.website ?: "",
                followerCount = rUser.followerCount ?: 0,
                followingCount = rUser.followingCount ?: 0,
                postCount = rUser.postCount ?: 0
            )

            _currentUser.value = loggedInUser

            // Save to Local Preferences
            prefs?.edit()?.apply {
                putString("access_token", token)
                putString("user_id", loggedInUser.id)
                putString("user_username", loggedInUser.username)
                putString("user_name", loggedInUser.name)
                putString("user_email", loggedInUser.email)
                putString("user_type", loggedInUser.userType.name)
                putBoolean("user_verified", loggedInUser.isVerified)
                putBoolean("user_farmer_badge", loggedInUser.hasFarmerBadge)
                apply()
            }

            fetchLiveData()
            null // Success
        } catch (e: Exception) {
            e.printStackTrace()
            val cleanError = e.localizedMessage ?: "Sunucu bağlantısı kurulamadı. Lütfen internetinizi kontrol edin."
            if (cleanError.contains("401")) {
                "Hatalı kullanıcı adı, e-posta veya şifre!"
            } else {
                cleanError
            }
        }
    }

    suspend fun register(email: String, name: String, username: String, password: String, userType: String): String? {
        return try {
            val response = apiService.register(RegisterRequest(email, name, password, username, userType))
            val token = response.token ?: return "Kayıt hatası: Token alınamadı."
            AgroLinkApiService.setToken(token)
            apiService = AgroLinkApiService.createClient()

            val rUser = response.user ?: return "Kullanıcı profili alınamadı."
            val uType = try {
                UserType.valueOf(rUser.userType ?: "FARMER")
            } catch (e: Exception) {
                UserType.FARMER
            }

            val registeredUser = User(
                id = rUser.id,
                username = rUser.username,
                name = rUser.name,
                email = rUser.email,
                profilePic = if (!rUser.profilePic.isNullOrBlank()) resolveUrl(rUser.profilePic) else "",
                coverPic = if (!rUser.coverPic.isNullOrBlank()) resolveUrl(rUser.coverPic) else "",
                bio = rUser.bio ?: "AgroLink ailesine yeni katıldı.",
                isVerified = rUser.isVerified ?: false,
                hasFarmerBadge = rUser.hasFarmerBadge ?: false,
                userType = uType,
                location = rUser.location ?: "Türkiye",
                website = rUser.website ?: "",
                followerCount = rUser.followerCount ?: 0,
                followingCount = rUser.followingCount ?: 0,
                postCount = rUser.postCount ?: 0
            )

            _currentUser.value = registeredUser

            prefs?.edit()?.apply {
                putString("access_token", token)
                putString("user_id", registeredUser.id)
                putString("user_username", registeredUser.username)
                putString("user_name", registeredUser.name)
                putString("user_email", registeredUser.email)
                putString("user_type", registeredUser.userType.name)
                putBoolean("user_verified", registeredUser.isVerified)
                putBoolean("user_farmer_badge", registeredUser.hasFarmerBadge)
                apply()
            }

            fetchLiveData()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.localizedMessage ?: "Kayıt işlemi başarısız oldu. Bilgileri gözden geçirin."
        }
    }

    suspend fun updateDeviceToken(fcmToken: String) {
        prefs?.edit()?.putString("fcm_token", fcmToken)?.apply()
        try {
            apiService.registerDeviceToken(mapOf("token" to fcmToken, "platform" to "android"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() {
        GlobalScope.launch {
            val token = prefs?.getString("fcm_token", null)
            if (token != null) {
                try {
                    apiService.deleteDeviceToken(mapOf("token" to token))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            prefs?.edit()?.clear()?.apply()
            AgroLinkApiService.setToken(null)
            apiService = AgroLinkApiService.createClient()
            _currentUser.value = null
        }
    }

    // POST INTERACTIONS
    fun toggleLikePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likeCount + 1 else post.likeCount - 1
                post.copy(isLiked = newLiked, likeCount = newCount)
            } else post
        }
        GlobalScope.launch {
            try {
                apiService.likePost(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleSavePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isSaved = !post.isSaved)
            } else post
        }
        GlobalScope.launch {
            try {
                apiService.savePost(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun voteInPoll(postId: String, optionId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId && post.isPoll) {
                val updatedOptions = post.pollOptions.map { option ->
                    if (option.id == optionId) {
                        option.copy(
                            votes = option.votes + if (!option.isVoted) 1 else 0,
                            isVoted = true
                        )
                    } else if (option.isVoted) {
                        option.copy(votes = (option.votes - 1).coerceAtLeast(0), isVoted = false)
                    } else option
                }
                post.copy(pollOptions = updatedOptions)
            } else post
        }
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        val me = _currentUser.value ?: return
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = me.id,
            username = me.username,
            profilePic = me.profilePic,
            content = content,
            createdAt = "Şimdi"
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    commentCount = post.commentCount + 1,
                    comments = post.comments + newComment
                )
            } else post
        }
    }

    fun createPost(content: String, location: String, isPoll: Boolean, pollQuestion: String, pollOptions: List<String>, imageBase64: String = "") {
        val me = _currentUser.value ?: return
        val media = if (imageBase64.isNotBlank()) listOf(imageBase64) else emptyList()
        val optionsList = if (isPoll) {
            pollOptions.filter { it.isNotBlank() }.map { PollOption(UUID.randomUUID().toString(), it, 0, false) }
        } else emptyList()

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            userId = me.id,
            authorUsername = me.username,
            authorName = me.name,
            authorProfilePic = me.profilePic,
            authorUserType = me.userType,
            hasVerifiedBadge = me.isVerified,
            content = content,
            mediaUrls = media,
            locationName = location,
            createdAt = "Şimdi",
            isPoll = isPoll,
            pollQuestion = pollQuestion,
            pollOptions = optionsList
        )
        _posts.value = listOf(newPost) + _posts.value

        _currentUser.value = me.copy(postCount = me.postCount + 1)

        GlobalScope.launch {
            try {
                apiService.createPost(
                    PostCreateRequest(
                        content = content,
                        locationName = location,
                        isPoll = isPoll,
                        pollQuestion = pollQuestion,
                        pollOptions = pollOptions.filter { it.isNotBlank() },
                        imageBase64 = if (imageBase64.isNotBlank()) imageBase64 else null,
                        mediaUrls = if (imageBase64.isNotBlank()) listOf(imageBase64) else null
                    )
                )
                loadFeed(refresh = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // STORY INTERACTIONS
    fun addStory(text: String, imageBase64: String) {
        val me = _currentUser.value ?: return
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val newStory = Story(
            id = UUID.randomUUID().toString(),
            userId = me.id,
            username = me.username,
            profilePic = me.profilePic,
            mediaUrl = imageBase64,
            text = text,
            createdAt = dateFormat.format(Date())
        )
        _stories.value = listOf(newStory) + _stories.value
    }

    // DM MESSAGING
    fun sendMessage(conversationId: String, content: String, isImage: Boolean = false, mediaUrl: String = "") {
        if (content.isBlank() && mediaUrl.isBlank()) return
        val me = _currentUser.value ?: return
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = me.id,
            content = content,
            timestamp = timestamp,
            isImage = isImage,
            isVoice = false,
            mediaUrl = mediaUrl
        )

        val currentMsgs = _messages.value[conversationId] ?: emptyList()
        val updatedMsgs = currentMsgs + newMessage
        _messages.value = _messages.value + (conversationId to updatedMsgs)

        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessage = if (isImage) "📷 Fotoğraf" else content,
                    lastMessageTime = timestamp,
                    unreadCount = 0
                )
            } else conv
        }

        val partnerId = _conversations.value.find { it.id == conversationId }?.partnerId ?: "u1"

        GlobalScope.launch {
            try {
                apiService.sendMessage(MessageCreateRequest(partnerId, content))
                val fetchedMsgs = apiService.getMessages(partnerId, 1, 50)
                if (fetchedMsgs.isNotEmpty()) {
                    _messages.value = _messages.value + (conversationId to fetchedMsgs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // FARMBOOK CRUD
    fun addFarmRecord(record: FarmRecord) {
        _farmRecords.value = listOf(record) + _farmRecords.value
        GlobalScope.launch {
            try {
                apiService.createFarmRecord(
                    FarmRecordCreateRequest(
                        recordType = record.recordType,
                        productName = record.productName,
                        fieldName = record.fieldName,
                        fieldSize = record.fieldSize,
                        fieldSizeUnit = record.fieldSizeUnit,
                        quantity = record.quantity,
                        unit = record.unit,
                        cost = record.cost,
                        income = record.income,
                        harvestAmount = record.harvestAmount,
                        harvestUnit = record.harvestUnit,
                        qualityRating = record.qualityRating,
                        weatherCondition = record.weatherCondition,
                        recordDate = record.recordDate,
                        season = record.season,
                        year = record.year,
                        notes = record.notes
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFarmRecord(id: String) {
        _farmRecords.value = _farmRecords.value.filter { it.id != id }
        GlobalScope.launch {
            try {
                apiService.deleteFarmRecord(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // FIELD TRACKER CRUD
    fun addFieldTracker(field: FieldTracker) {
        _fieldTrackers.value = _fieldTrackers.value + field
    }

    // DISEASE ALARMS CRUD
    fun addDiseaseAlarm(alarm: DiseaseAlarm) {
        _diseaseAlarms.value = listOf(alarm) + _diseaseAlarms.value
        GlobalScope.launch {
            try {
                apiService.createDiseaseAlarm(
                    DiseaseAlarmCreateRequest(
                        cropType = alarm.cropType,
                        diseaseInfo = alarm.diseaseInfo,
                        city = alarm.city,
                        district = alarm.district,
                        note = alarm.note
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // CROP PRICE WATCH
    fun toggleFollowPrice(priceId: String) {
        _cropPrices.value = _cropPrices.value.map { price ->
            if (price.id == priceId) price.copy(isFollowed = !price.isFollowed) else price
        }
    }

    // PRODUCTS CRUD
    fun addProduct(product: Product) {
        _products.value = listOf(product) + _products.value
        GlobalScope.launch {
            try {
                apiService.createProduct(
                    ProductCreateRequest(
                        name = product.name,
                        description = product.description,
                        price = product.price,
                        category = product.category,
                        stock = product.stock,
                        stockUnit = product.stockUnit,
                        imageBase64 = product.imageBase64,
                        location = product.location
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // USER PROFILE EDIT
    fun updateProfile(name: String, bio: String, location: String, website: String, profilePicBase64: String = "", coverPicBase64: String = "") {
        val me = _currentUser.value ?: return
        _currentUser.value = me.copy(
            name = name,
            bio = bio,
            location = location,
            website = website,
            profilePic = if (profilePicBase64.isNotBlank()) profilePicBase64 else me.profilePic,
            coverPic = if (coverPicBase64.isNotBlank()) coverPicBase64 else me.coverPic
        )
        GlobalScope.launch {
            try {
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationBody = location.toRequestBody("text/plain".toMediaTypeOrNull())
                val websiteBody = website.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val profilePart = if (profilePicBase64.isNotBlank()) {
                    try {
                        val cleanBase64 = if (profilePicBase64.contains(",")) profilePicBase64.split(",")[1] else profilePicBase64
                        val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        okhttp3.MultipartBody.Part.createFormData("profilePic", "profile.jpg", requestFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else null

                apiService.updateProfileMultipart(
                    name = nameBody,
                    bio = bioBody,
                    location = locationBody,
                    website = websiteBody,
                    profilePic = profilePart
                )
                fetchLiveData()
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to legacy endpoint if multipart fails
                try {
                    apiService.updateProfile(
                        UpdateProfileRequest(
                            name = name,
                            bio = bio,
                            location = location,
                            website = website,
                            profilePic = if (profilePicBase64.isNotBlank()) profilePicBase64 else null,
                            coverPic = if (coverPicBase64.isNotBlank()) coverPicBase64 else null
                        )
                    )
                    fetchLiveData()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun deleteProfilePicture(type: String) {
        GlobalScope.launch {
            try {
                apiService.deleteProfilePic(type)
                fetchLiveData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // APPLY FOR GOLD CERTIFICATE / BLUE VERIFIED
    fun applyForVerification() {
        val me = _currentUser.value ?: return
        _currentUser.value = me.copy(isVerified = true, hasFarmerBadge = true)
    }
}
