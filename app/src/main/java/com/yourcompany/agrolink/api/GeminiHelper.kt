package com.yourcompany.agrolink.api

import android.util.Log
import com.yourcompany.agrolink.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {

    private const val TAG = "GeminiHelper"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a chat prompt to Gemini 3.5 Flash or uses fallback agricultural knowledge if key is missing/fails.
     */
    suspend fun getAgriculturalAdvice(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is missing, using local agricultural fallback.")
            return@withContext getLocalAgriculturalFallback(userMessage)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            // Build request JSON manually using standard JSONObject
            val partObject = JSONObject().put("text", "Sen AgroLink platformunun uzman Ziraat Mühendisi yapay zeka asistanısın. Çiftçilere, akademisyenlere ve tarım meraklılarına bilimsel, pratik ve Türkçe yanıtlar vermelisin. Soru: $userMessage")
            val partsArray = JSONArray().put(partObject)
            val contentObject = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObject)
            
            val requestBodyJson = JSONObject().put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed with code ${response.code}: ${response.message}")
                    return@withContext getLocalAgriculturalFallback(userMessage)
                }

                val responseBodyStr = response.body?.string() ?: ""
                if (responseBodyStr.isBlank()) {
                    return@withContext getLocalAgriculturalFallback(userMessage)
                }

                // Parse the response candidates
                val jsonObject = JSONObject(responseBodyStr)
                val candidates = jsonObject.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val partsArr = contentObj.optJSONArray("parts")
                        if (partsArr != null && partsArr.length() > 0) {
                            return@withContext partsArr.getJSONObject(0).optString("text", "Yanıt alınamadı.")
                        }
                    }
                }
                return@withContext getLocalAgriculturalFallback(userMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini request: ${e.message}", e)
            return@withContext getLocalAgriculturalFallback(userMessage)
        }
    }

    private fun getLocalAgriculturalFallback(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("pas") || query.contains("sarı pas") || query.contains("pas hastalığı") -> {
                "🌾 *Sarı Pas Hastalığı (Puccinia striiformis) Teşhisi ve Mücadelesi*\n\n" +
                "Sarı pas, özellikle serin ve nemli ilkbahar aylarında buğday yapraklarında sarımsı püstüller halinde başlar. Rüzgarla hızla yayılır.\n\n" +
                "🛡️ *Çözüm Önerileri:*\n" +
                "1. *Kimyasal Mücadele:* Hastalık belirtileri tarlada ilk görüldüğünde vakit kaybetmeden Triazole veya Strobilurin grubu (örn: Tebuconazole) fungusitlerle ilaçlama yapmalısınız.\n" +
                "2. *Kültürel Önlemler:* Sık ekimden kaçının, tarlayı aşırı azotla gübrelemeyin, pas hastalığına dayanıklı tohum çeşitlerini (örn: yerli sertifikalı tohumlar) tercih edin."
            }
            query.contains("koçan") || query.contains("rastık") || query.contains("mısır") && query.contains("siyah") -> {
                "🌽 *Mısır Rastığı (Ustilago maydis) Mücadelesi*\n\n" +
                "Mısır koçanlarında, saplarında veya yapraklarında beliren gümüşi-beyaz renkli, içi siyah spor dolu şişkinliklerdir.\n\n" +
                "🛡️ *Çözüm Önerileri:*\n" +
                "1. *Mekanik Mücadele:* Hastalıklı şişkinlikleri (galleri) patlamadan önce tarladan kesip çıkarın ve derin bir yere gömün veya yakın.\n" +
                "2. *Kültürel Önlemler:* Tarlada mısır kurdu zararlısıyla mücadele edin (çünkü yaralardan bulaşır). Hastalıklı alanlarda ekim nöbeti (münavebe) uygulayarak mısır yerine baklagil ekin."
            }
            query.contains("domates") || query.contains("erken yanıklık") || query.contains("yaprak yanıklığı") -> {
                "🍅 *Domates Erken Yaprak Yanıklığı (Alternaria solani) Tedavisi*\n\n" +
                "Genelde alt yapraklarda eş merkezli halkalar içeren kahverengi-siyah lekelerle kendini gösterir.\n\n" +
                "🛡️ *Çözüm Önerileri:*\n" +
                "1. *İlaçlama:* Bakırlı preparatlar (Bakır Hidroksit vb.) veya sistemik fungusitlerle (örn: Azoxystrobin) düzenli aralıklarla yapraktan püskürtme yapın.\n" +
                "2. *Sulama:* Damlama sulama kullanın, yaprakları kesinlikle ıslatmayın."
            }
            query.contains("gübre") || query.contains("gübreleme") || query.contains("azot") -> {
                "🧪 *AgroLink Dengeli Gübreleme Rehberi*\n\n" +
                "Toprak analizi yaptırmadan gübre atmak hem maliyetli hem de toprağa zararlıdır. Genel olarak:\n" +
                "- *Ekim öncesi (Taban gübresi):* Fosfor ve potasyum ağırlıklı (örn: DAP 18-46-0 veya 15-15-15 kompoze gübre) tercih edilir.\n" +
                "- *Kardeşlenme/Gelişme dönemi (Üst gübreleme):* Azot ağırlıklı (örn: Üre %46 veya Amonyum Nitrat %26-%33) miktarında atılmalıdır."
            }
            query.contains("solucan") || query.contains("organik gübre") -> {
                "🪱 *Solucan Gübresi ve Organik Tarım Faydaları*\n\n" +
                "Solucan gübresi toprağın su tutma kapasitesini artırır, kök gelişimini hızlandırır ve mikroorganizmaları aktive eder.\n\n" +
                "💡 *Uygulama:* Sebzelerde fide dikimi sırasında kök çukuruna bir avuç (yaklaşık 100g) koyulması verimi %30'a kadar artıracaktır."
            }
            query.contains("merhaba") || query.contains("selam") || query.contains("kimsin") -> {
                "🌱 *Merhaba! Ben AgroLink AI Tarım Danışmanınız.*\n\n" +
                "Karatay'dan Çukurova'ya kadar tüm tarım arazilerinde karşılaştığınız sorunlarda yanınızdayım!\n\n" +
                "Bana şunları sorabilirsiniz:\n" +
                "👉 *'Mısır koçanındaki siyah lekeler ne?'*\n" +
                "👉 *'Buğdayda sarı pasla nasıl mücadele edilir?'*\n" +
                "👉 *'Toprağın azot miktarını nasıl artırırım?'*"
            }
            else -> {
                "🚜 *Değerli AgroLink Üreticisi,*\n\n" +
                "Sorduğunuz konu tarımsal verimliliğinizi doğrudan etkileyebilir. Konuyu daha iyi anlamam için bölgenizi, yetiştirdiğiniz ürünü ve belirtileri (örn: yapraklarda kuruma, kökte çürüme) detaylandırabilir misiniz?\n\n" +
                "🔬 Her ihtimale karşı tarlanızdan yaprak numunesi alarak sistemdeki ziraat mühendislerimize (örn: @ziraat_müh_merve) danışmanızı veya en yakın İlçe Tarım Müdürlüğü'ne toprak/yaprak analizi yaptırmanızı öneririm."
            }
        }
    }
}
