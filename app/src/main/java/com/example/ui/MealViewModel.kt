package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiMealAssistant
import com.example.api.NeisApiClient
import com.example.data.AppDatabase
import com.example.data.FavoriteDishEntity
import com.example.data.MealRatingEntity
import com.example.model.MealInfo
import com.example.model.SchoolInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String = "급식 정보가 없습니다.") : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val mealDao = db.mealDao()
    private val prefs = application.getSharedPreferences("daejin_meal_prefs", Context.MODE_PRIVATE)

    // Current Selected School
    private val _currentSchool = MutableStateFlow(loadSavedSchool())
    val currentSchool: StateFlow<SchoolInfo> = _currentSchool.asStateFlow()

    // Selected Date
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Selected Meal Type ("1": 조식, "2": 중식, "3": 석식)
    private val _selectedMealCode = MutableStateFlow("2")
    val selectedMealCode: StateFlow<String> = _selectedMealCode.asStateFlow()

    // Daily Meals UiState
    private val _mealsState = MutableStateFlow<UiState<List<MealInfo>>>(UiState.Loading)
    val mealsState: StateFlow<UiState<List<MealInfo>>> = _mealsState.asStateFlow()

    // Monthly Meals Map
    private val _monthlyMeals = MutableStateFlow<Map<String, List<MealInfo>>>(emptyMap())
    val monthlyMeals: StateFlow<Map<String, List<MealInfo>>> = _monthlyMeals.asStateFlow()

    private val _isMonthlyLoading = MutableStateFlow(false)
    val isMonthlyLoading: StateFlow<Boolean> = _isMonthlyLoading.asStateFlow()

    // Favorite Dishes List
    private val _favoriteDishes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteDishes: StateFlow<Set<String>> = _favoriteDishes.asStateFlow()

    // User Selected Allergies (Codes 1 to 19)
    private val _userAllergies = MutableStateFlow<List<Int>>(loadSavedAllergies())
    val userAllergies: StateFlow<List<Int>> = _userAllergies.asStateFlow()

    // Ratings List
    private val _ratingsList = MutableStateFlow<List<MealRatingEntity>>(emptyList())
    val ratingsList: StateFlow<List<MealRatingEntity>> = _ratingsList.asStateFlow()

    // AI Analysis Result
    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // School Search
    private val _schoolSearchResults = MutableStateFlow<List<SchoolInfo>>(emptyList())
    val schoolSearchResults: StateFlow<List<SchoolInfo>> = _schoolSearchResults.asStateFlow()

    private val _isSearchingSchool = MutableStateFlow(false)
    val isSearchingSchool: StateFlow<Boolean> = _isSearchingSchool.asStateFlow()

    init {
        // Collect favorites
        viewModelScope.launch {
            mealDao.getAllFavoriteDishes().collectLatest { list ->
                _favoriteDishes.value = list.map { it.dishName }.toSet()
            }
        }
        // Collect ratings
        viewModelScope.launch {
            mealDao.getAllRatings().collectLatest { list ->
                _ratingsList.value = list
            }
        }

        // Fetch initial meals
        loadMealsForDate(_selectedDate.value)
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _aiAnalysis.value = null
        loadMealsForDate(date)
    }

    fun setSelectedMealCode(code: String) {
        _selectedMealCode.value = code
    }

    fun loadMealsForDate(date: LocalDate) {
        viewModelScope.launch {
            _mealsState.value = UiState.Loading
            val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val school = _currentSchool.value

            val list = NeisApiClient.getMeals(
                officeCode = school.officeCode,
                schoolCode = school.schoolCode,
                date = dateStr
            )

            if (list.isEmpty()) {
                _mealsState.value = UiState.Empty("해당 날짜(${date.monthValue}월 ${date.dayOfMonth}일)에 등록된 급식 정보가 없습니다. (주말 또는 휴교일)")
            } else {
                _mealsState.value = UiState.Success(list)
            }
        }
    }

    fun loadMealsForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _isMonthlyLoading.value = true
            val firstDay = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val lastDay = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val school = _currentSchool.value

            val list = NeisApiClient.getMeals(
                officeCode = school.officeCode,
                schoolCode = school.schoolCode,
                fromDate = firstDay,
                toDate = lastDay
            )

            val map = list.groupBy { it.date }
            _monthlyMeals.value = map
            _isMonthlyLoading.value = false
        }
    }

    fun toggleFavoriteDish(dishName: String) {
        viewModelScope.launch {
            if (_favoriteDishes.value.contains(dishName)) {
                mealDao.deleteFavoriteDishByName(dishName)
            } else {
                mealDao.insertFavoriteDish(FavoriteDishEntity(dishName = dishName))
            }
        }
    }

    fun toggleAllergy(allergyCode: Int) {
        val current = _userAllergies.value.toMutableList()
        if (current.contains(allergyCode)) {
            current.remove(allergyCode)
        } else {
            current.add(allergyCode)
        }
        _userAllergies.value = current
        saveAllergies(current)
    }

    fun analyzeMealWithAi(meal: MealInfo) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiAnalysis.value = null
            val result = GeminiMealAssistant.analyzeMeal(meal, _userAllergies.value)
            _aiAnalysis.value = result
            _isAiLoading.value = false
        }
    }

    fun saveRating(meal: MealInfo, rating: Float, comment: String) {
        viewModelScope.launch {
            val key = "${meal.date}_${meal.mealCode}"
            val entity = MealRatingEntity(
                dateMealKey = key,
                date = meal.date,
                mealCode = meal.mealCode,
                mealName = meal.mealName,
                rating = rating,
                comment = comment
            )
            mealDao.saveRating(entity)
        }
    }

    fun searchSchools(query: String) {
        viewModelScope.launch {
            if (query.trim().isEmpty()) {
                _schoolSearchResults.value = emptyList()
                return@launch
            }
            _isSearchingSchool.value = true
            val results = NeisApiClient.searchSchool(query)
            _schoolSearchResults.value = results
            _isSearchingSchool.value = false
        }
    }

    fun setSchool(school: SchoolInfo) {
        _currentSchool.value = school
        saveSchool(school)
        _schoolSearchResults.value = emptyList()
        loadMealsForDate(_selectedDate.value)
    }

    fun resetToDaejinHighSchool() {
        setSchool(SchoolInfo.DAEJIN_HIGH_SCHOOL)
    }

    private fun saveSchool(school: SchoolInfo) {
        prefs.edit()
            .putString("officeCode", school.officeCode)
            .putString("officeName", school.officeName)
            .putString("schoolCode", school.schoolCode)
            .putString("schoolName", school.schoolName)
            .putString("address", school.address)
            .putString("phone", school.phone)
            .putString("website", school.website)
            .apply()
    }

    private fun loadSavedSchool(): SchoolInfo {
        val schoolCode = prefs.getString("schoolCode", null)
        if (schoolCode == null) return SchoolInfo.DAEJIN_HIGH_SCHOOL

        return SchoolInfo(
            officeCode = prefs.getString("officeCode", "C10") ?: "C10",
            officeName = prefs.getString("officeName", "부산광역시교육청") ?: "부산광역시교육청",
            schoolCode = schoolCode,
            schoolName = prefs.getString("schoolName", "대진전자통신고등학교") ?: "대진전자통신고등학교",
            address = prefs.getString("address", "부산광역시 금정구 수림로 92 (장전동)") ?: "",
            phone = prefs.getString("phone", "051-582-8100") ?: "",
            website = prefs.getString("website", "www.pdj.hs.kr") ?: ""
        )
    }

    private fun saveAllergies(list: List<Int>) {
        val str = list.joinToString(",")
        prefs.edit().putString("user_allergies", str).apply()
    }

    private fun loadSavedAllergies(): List<Int> {
        val str = prefs.getString("user_allergies", null) ?: return emptyList()
        if (str.isBlank()) return emptyList()
        return str.split(",").mapNotNull { it.toIntOrNull() }
    }
}
