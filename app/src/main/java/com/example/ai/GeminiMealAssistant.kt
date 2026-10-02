package com.example.ai

import com.example.BuildConfig
import com.example.model.MealInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration

object GeminiMealAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(12))
        .readTimeout(Duration.ofSeconds(12))
        .build()

    suspend fun analyzeMeal(meal: MealInfo, userAllergies: List<Int>): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }

        val dishListStr = meal.dishes.joinToString(", ") { it.name }
        val prompt = """
            당신은 학교 급식 전문 친절한 AI 영양사입니다.
            대진전자통신고등학교의 오늘 급식 메뉴(${meal.mealName})를 평가해주세요:
            
            [오늘의 메뉴]
            $dishListStr
            칼로리: ${meal.calorie}
            
            다음 내용을 포함하여 학생들이 읽기 쉬운 다정한 말투(해요체)로 3~4줄로 분석해주세요:
            1. 영양 균형 점수 (100점 만점) 및 한줄평
            2. 이 급식의 주요 건강 이점 (탄수화물, 단백질, 비타민 등)
            3. 오후 피로 회복을 위한 팁 또는 저녁 식사 추천
        """.trimIndent()

        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val root = JSONObject(responseStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotBlank()) return@withContext text
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback smart AI Nutritionist summary generator
        generateLocalSmartAnalysis(meal, userAllergies)
    }

    private fun generateLocalSmartAnalysis(meal: MealInfo, userAllergies: List<Int>): String {
        val dishNames = meal.dishes.map { it.name }
        val hasProtein = dishNames.any { it.contains("불고기") || it.contains("고기") || it.contains("닭") || it.contains("치킨") || it.contains("돈육") || it.contains("계란") || it.contains("두부") }
        val hasSoup = dishNames.any { it.contains("국") || it.contains("찌개") || it.contains("탕") }
        val hasFruitOrDessert = dishNames.any { it.contains("과일") || it.contains("바나나") || it.contains("사과") || it.contains("요거트") || it.contains("주스") || it.contains("우유") }

        val score = if (hasProtein && hasSoup) "95점" else "88점"

        val sb = StringBuilder()
        sb.append("🥗 [AI 영양사 영양 평가: ").append(score).append("]\n")
        sb.append("오늘의 ").append(meal.mealName).append("은(는) ")

        if (hasProtein) {
            sb.append("단백질이 풍부하여 오후 수업과 학업 집중력 향상에 큰 도움을 줍니다! ")
        } else {
            sb.append("탄수화물과 섬유질이 균형 있게 구성되어 속이 편안합니다. ")
        }

        if (hasSoup) {
            sb.append("따뜻한 국물 요리로 영양 흡수를 돕고 식감이 한층 원활해요. ")
        }

        if (hasFruitOrDessert) {
            sb.append("후식으로 제공되는 과일/디저트로 비타민 C를 챙길 수 있습니다! ")
        }

        sb.append("\n💡 팁: 칼로리는 ").append(meal.calorie.ifEmpty { "적정 수준" }).append("으로, 저녁에는 가벼운 신체 활동이나 정갈한 밥상으로 마무리하시면 아주 좋습니다. 맛있게 드세요! 😊")

        return sb.toString()
    }
}
