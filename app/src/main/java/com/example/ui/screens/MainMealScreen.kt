package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MealInfo
import com.example.ui.MealViewModel
import com.example.ui.UiState
import com.example.ui.components.DateNavigationBar
import com.example.ui.components.MealCard

@Composable
fun MainMealScreen(
    viewModel: MealViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val school by viewModel.currentSchool.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMealCode by viewModel.selectedMealCode.collectAsState()
    val mealsState by viewModel.mealsState.collectAsState()
    val favoriteDishes by viewModel.favoriteDishes.collectAsState()
    val userAllergies by viewModel.userAllergies.collectAsState()
    val ratingsList by viewModel.ratingsList.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero School Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Background Banner Image
            Image(
                painter = painterResource(id = R.drawable.school_cafeteria_banner_1790925833683),
                contentDescription = "대진전자통신고등학교 급식실",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // School Info Overlay Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = school.schoolName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${school.officeName} • ${school.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    // School Homepage / Call Quick Actions
                    Row {
                        if (school.phone.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${school.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "전화 걸기",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (school.website.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val webUrl = if (school.website.startsWith("http")) school.website else "https://${school.website}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "학교 홈페이지",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Date Navigation Bar
        DateNavigationBar(
            selectedDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) }
        )

        // Meal Category Selector Tabs: [조식] [중식] [석식]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mealOptions = listOf("1" to "조식 🌅", "2" to "중식 ☀️", "3" to "석식 🌙")
            mealOptions.forEach { (code, label) ->
                FilterChip(
                    selected = selectedMealCode == code,
                    onClick = { viewModel.setSelectedMealCode(code) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedMealCode == code) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_meal_$code")
                )
            }
        }

        // Main Content Area based on UiState
        when (val state = mealsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "급식 정보를 불러오는 중입니다...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is UiState.Empty -> {
                EmptyMealCard(
                    message = state.message,
                    onRetry = { viewModel.loadMealsForDate(selectedDate) }
                )
            }

            is UiState.Error -> {
                EmptyMealCard(
                    message = state.message,
                    onRetry = { viewModel.loadMealsForDate(selectedDate) }
                )
            }

            is UiState.Success -> {
                val meals = state.data
                val filteredMeal = meals.find { it.mealCode == selectedMealCode }

                if (filteredMeal == null) {
                    val availableNames = meals.joinToString(", ") { it.mealName }
                    EmptyMealCard(
                        message = "선택하신 식사(${mealNameFromCode(selectedMealCode)}) 정보가 없습니다.\n(오늘 등록된 식사: $availableNames)",
                        onRetry = { viewModel.loadMealsForDate(selectedDate) }
                    )
                } else {
                    val ratingKey = "${filteredMeal.date}_${filteredMeal.mealCode}"
                    val currentRating = ratingsList.find { it.dateMealKey == ratingKey }

                    MealCard(
                        meal = filteredMeal,
                        favoriteDishes = favoriteDishes,
                        userAllergies = userAllergies,
                        currentRating = currentRating,
                        onToggleFavorite = { viewModel.toggleFavoriteDish(it) },
                        onSaveRating = { rating, comment ->
                            viewModel.saveRating(filteredMeal, rating, comment)
                        }
                    )

                    // AI Nutrition Analysis Section
                    AiNutritionistSection(
                        meal = filteredMeal,
                        aiAnalysis = aiAnalysis,
                        isLoading = isAiLoading,
                        onAnalyze = { viewModel.analyzeMealWithAi(filteredMeal) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmptyMealCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LunchDining,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.testTag("btn_retry_fetch_meal")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("다시 조회하기")
            }
        }
    }
}

@Composable
fun AiNutritionistSection(
    meal: MealInfo,
    aiAnalysis: String?,
    isLoading: Boolean,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("card_ai_nutritionist"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "AI 영양사 리포트",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (aiAnalysis == null) {
                    Button(
                        onClick = onAnalyze,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("btn_analyze_ai_meal")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("한줄 분석 받기")
                        }
                    }
                }
            }

            AnimatedVisibility(visible = aiAnalysis != null) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiAnalysis ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun mealNameFromCode(code: String): String {
    return when (code) {
        "1" -> "조식"
        "2" -> "중식"
        "3" -> "석식"
        else -> "급식"
    }
}
