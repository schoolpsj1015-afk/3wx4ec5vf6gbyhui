package com.example.model

data class MealInfo(
    val date: String,             // Format: YYYYMMDD (e.g. "20261002")
    val mealCode: String,         // "1": 조식, "2": 중식, "3": 석식
    val mealName: String,         // "조식", "중식", "석식"
    val dishes: List<DishItem>,
    val calorie: String = "",     // e.g. "782.5 Kcal"
    val originInfo: String = "",  // 원산지 정보
    val nutritionInfo: String = "",// 영양 성분 정보
    val rawDishString: String = ""
) {
    fun getFormattedDate(): String {
        if (date.length == 8) {
            val year = date.substring(0, 4)
            val month = date.substring(4, 6)
            val day = date.substring(6, 8)
            return "${year}년 ${month}월 ${day}일"
        }
        return date
    }

    fun hasAllergyWarning(userAllergies: List<Int>): Boolean {
        if (userAllergies.isEmpty()) return false
        return dishes.any { dish ->
            dish.allergyCodes.any { code -> userAllergies.contains(code) }
        }
    }

    fun getAllergyListForUser(userAllergies: List<Int>): List<String> {
        val triggered = mutableSetOf<String>()
        dishes.forEach { dish ->
            dish.allergyCodes.forEach { code ->
                if (userAllergies.contains(code)) {
                    AllergyInfo.ALLERGY_MAP[code]?.let { triggered.add(it) }
                }
            }
        }
        return triggered.toList()
    }
}
