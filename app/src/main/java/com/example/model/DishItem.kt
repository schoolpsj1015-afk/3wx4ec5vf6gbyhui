package com.example.model

data class DishItem(
    val name: String,
    val allergyCodes: List<Int> = emptyList(),
    val isFavorite: Boolean = false
) {
    fun getAllergyNames(): List<String> {
        return allergyCodes.mapNotNull { AllergyInfo.ALLERGY_MAP[it] }
    }
}

object AllergyInfo {
    val ALLERGY_MAP = mapOf(
        1 to "난류",
        2 to "우유",
        3 to "메밀",
        4 to "땅콩",
        5 to "대두",
        6 to "밀",
        7 to "고등어",
        8 to "게",
        9 to "새우",
        10 to "돼지고기",
        11 to "복숭아",
        12 to "토마토",
        13 to "아황산류",
        14 to "호두",
        15 to "닭고기",
        16 to "쇠고기",
        17 to "오징어",
        18 to "조개류",
        19 to "잣"
    )

    fun parseDishLine(line: String): DishItem {
        val cleanLine = line.trim()
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
        
        // NEIS dish format: "돈육불고기(1.5.6.10)" or "쌀밥" or "미역국 (5.6)"
        val allergyRegex = Regex("""\(([\d\.]+)\)""")
        val match = allergyRegex.find(cleanLine)
        
        return if (match != null) {
            val dishName = cleanLine.replace(match.value, "").trim()
            val codes = match.groupValues[1].split(".")
                .mapNotNull { it.toIntOrNull() }
            DishItem(name = dishName, allergyCodes = codes)
        } else {
            DishItem(name = cleanLine, allergyCodes = emptyList())
        }
    }
}
