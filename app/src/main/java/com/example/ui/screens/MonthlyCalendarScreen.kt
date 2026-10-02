package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MealViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthlyCalendarScreen(
    viewModel: MealViewModel,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val monthlyMeals by viewModel.monthlyMeals.collectAsState()
    val isMonthlyLoading by viewModel.isMonthlyLoading.collectAsState()

    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    // Fetch month meals whenever currentYearMonth changes
    LaunchedEffect(currentYearMonth) {
        viewModel.loadMealsForMonth(currentYearMonth)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Month Navigation Header
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    modifier = Modifier.testTag("btn_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "이전 달"
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    modifier = Modifier.testTag("btn_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "다음 달"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday Header Row (일 월 화 수 목 금 토)
        val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdays.forEachIndexed { index, dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = when (index) {
                        0 -> MaterialTheme.colorScheme.error // Sunday red
                        6 -> MaterialTheme.colorScheme.primary // Saturday blue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isMonthlyLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Calendar Grid Calculation
            val firstDayOfMonth = currentYearMonth.atDay(1)
            val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
            val daysInMonth = currentYearMonth.lengthOfMonth()

            val totalCells = dayOfWeekValue + daysInMonth

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(totalCells) { index ->
                    if (index < dayOfWeekValue) {
                        // Empty leading cells
                        Spacer(modifier = Modifier.aspectRatio(0.65f))
                    } else {
                        val dayNumber = index - dayOfWeekValue + 1
                        val date = currentYearMonth.atDay(dayNumber)
                        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

                        val dayMeals = monthlyMeals[dateStr] ?: emptyList()
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate
                        val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

                        // Prioritize 중식 (mealCode "2"), else any available meal
                        val targetMeal = dayMeals.find { it.mealCode == "2" } ?: dayMeals.firstOrNull()
                        val previewDishes = targetMeal?.dishes?.map { it.name } ?: emptyList()

                        CalendarDayCell(
                            dayNumber = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            isWeekend = isWeekend,
                            previewDishes = previewDishes,
                            onClick = { onDayClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    previewDishes: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day Number Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$dayNumber",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        isWeekend -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 11.sp
                )
            }

            // Preview dishes list (1 line per dish, up to 4 dishes)
            if (previewDishes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    previewDishes.take(4).forEach { dishName ->
                        Text(
                            text = dishName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
