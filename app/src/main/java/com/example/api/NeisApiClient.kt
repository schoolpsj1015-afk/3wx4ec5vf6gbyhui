package com.example.api

import com.example.model.AllergyInfo
import com.example.model.DishItem
import com.example.model.MealInfo
import com.example.model.SchoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Duration

object NeisApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(10))
        .build()

    private const val BASE_URL = "https://open.neis.go.kr/hub"

    /**
     * Fetch meals for a specific school and date (or date range)
     * Handles pagination to ensure complete results for monthly queries
     */
    suspend fun getMeals(
        officeCode: String = "C10",
        schoolCode: String = "7150597",
        date: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        apiKey: String? = null
    ): List<MealInfo> = withContext(Dispatchers.IO) {
        val allMeals = mutableListOf<MealInfo>()
        var pIndex = 1
        val pSize = 1000

        while (true) {
            val urlBuilder = StringBuilder("$BASE_URL/mealServiceDietInfo?Type=json&pIndex=$pIndex&pSize=$pSize")
            urlBuilder.append("&ATPT_OFCDC_SC_CODE=").append(officeCode)
            urlBuilder.append("&SD_SCHUL_CODE=").append(schoolCode)

            if (!apiKey.isNull_or_blank_check(apiKey)) {
                urlBuilder.append("&KEY=").append(apiKey)
            }

            if (date != null) {
                urlBuilder.append("&MLSV_YMD=").append(date)
            } else if (fromDate != null && toDate != null) {
                urlBuilder.append("&MLSV_FROM_YMD=").append(fromDate)
                urlBuilder.append("&MLSV_TO_YMD=").append(toDate)
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build()

            val pageMeals = try {
                val response = client.newCall(request).execute()
                val jsonString = response.body?.string() ?: ""
                parseMealJson(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            if (pageMeals.isEmpty()) break
            allMeals.addAll(pageMeals)

            // If single date query or returned less than pSize, we fetched all data
            if (date != null || pageMeals.size < pSize) break
            pIndex++
        }

        allMeals
    }

    private fun String?.isNull_or_blank_check(str: String?): Boolean {
        return str == null || str.trim().isEmpty()
    }

    private fun parseMealJson(jsonString: String): List<MealInfo> {
        val resultList = mutableListOf<MealInfo>()
        try {
            val root = JSONObject(jsonString)
            if (!root.has("mealServiceDietInfo")) {
                return emptyList()
            }

            val array = root.getJSONArray("mealServiceDietInfo")
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                if (item.has("row")) {
                    val rows = item.getJSONArray("row")
                    for (j in 0 until rows.length()) {
                        val row = rows.getJSONObject(j)
                        val ymd = row.optString("MLSV_YMD", "")
                        val mmealCode = row.optString("MMEAL_SC_CODE", "2")
                        val mmealName = row.optString("MMEAL_SC_NM", "중식")
                        val rawDish = row.optString("DDISH_NM", "")
                        val calInfo = row.optString("CAL_INFO", "")
                        val originInfo = row.optString("ORGRP_INFO", "").replace("<br/>", "\n").replace("<br>", "\n")
                        val ntrInfo = row.optString("NTR_INFO", "").replace("<br/>", "\n").replace("<br>", "\n")

                        // Clean raw dish lines
                        val rawLines = rawDish.split("<br/>", "<br>", "\n")
                        val dishes = rawLines
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .map { AllergyInfo.parseDishLine(it) }

                        resultList.add(
                            MealInfo(
                                date = ymd,
                                mealCode = mmealCode,
                                mealName = mmealName,
                                dishes = dishes,
                                calorie = calInfo,
                                originInfo = originInfo,
                                nutritionInfo = ntrInfo,
                                rawDishString = rawDish
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    /**
     * Search school list by name across Korea
     */
    suspend fun searchSchool(query: String): List<SchoolInfo> = withContext(Dispatchers.IO) {
        if (query.trim().isEmpty()) return@withContext emptyList()
        val url = "$BASE_URL/schoolInfo?Type=json&pIndex=1&pSize=30&SCHUL_NM=${query.trim()}"

        val request = Request.Builder().url(url).get().build()
        try {
            val response = client.newCall(request).execute()
            val jsonString = response.body?.string() ?: return@withContext emptyList()
            parseSchoolJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseSchoolJson(jsonString: String): List<SchoolInfo> {
        val list = mutableListOf<SchoolInfo>()
        try {
            val root = JSONObject(jsonString)
            if (!root.has("schoolInfo")) return emptyList()

            val array = root.getJSONArray("schoolInfo")
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                if (item.has("row")) {
                    val rows = item.getJSONArray("row")
                    for (j in 0 until rows.length()) {
                        val row = rows.getJSONObject(j)
                        val officeCode = row.optString("ATPT_OFCDC_SC_CODE", "")
                        val officeName = row.optString("ATPT_OFCDC_SC_NM", "")
                        val schoolCode = row.optString("SD_SCHUL_CODE", "")
                        val schoolName = row.optString("SCHUL_NM", "")
                        val engSchoolName = row.optString("ENG_SCHUL_NM", "")
                        val schoolType = row.optString("SCHUL_KND_SC_NM", "")
                        val locationName = row.optString("LCTN_SC_NM", "")
                        val address = row.optString("ORG_RDNMA", "") + " " + row.optString("ORG_RDNDA", "")
                        val phone = row.optString("ORG_TELNO", "")
                        val website = row.optString("HPG_ADRES", "")
                        val fax = row.optString("ORG_FAXNO", "")
                        val foundingDate = row.optString("FOND_YMD", "")

                        list.add(
                            SchoolInfo(
                                officeCode = officeCode,
                                officeName = officeName,
                                schoolCode = schoolCode,
                                schoolName = schoolName,
                                engSchoolName = engSchoolName,
                                schoolType = schoolType,
                                locationName = locationName,
                                address = address.trim(),
                                phone = phone,
                                website = website,
                                fax = fax,
                                foundingDate = foundingDate
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
