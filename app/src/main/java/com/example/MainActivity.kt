package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.MealViewModel
import com.example.ui.screens.FavoritesAndRatingsScreen
import com.example.ui.screens.MainMealScreen
import com.example.ui.screens.MonthlyCalendarScreen
import com.example.ui.screens.SchoolAndSettingsScreen
import com.example.ui.theme.DaejinMealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaejinMealTheme {
                val viewModel: MealViewModel = viewModel()
                DaejinMealApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    DAILY("오늘 급식", Icons.Default.Restaurant, "tab_nav_daily"),
    MONTHLY("월간 달력", Icons.Default.CalendarMonth, "tab_nav_monthly"),
    FAVORITES("즐겨찾기/후기", Icons.Default.Star, "tab_nav_favorites"),
    SETTINGS("학교정보/설정", Icons.Default.School, "tab_nav_settings")
}

@Composable
fun DaejinMealApp(viewModel: MealViewModel) {
    var selectedTabOrdinal by remember { mutableIntStateOf(0) }
    val tabs = NavTab.values()

    // Handle back button on sub-screens
    if (selectedTabOrdinal != 0) {
        BackHandler {
            selectedTabOrdinal = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabOrdinal == index,
                        onClick = { selectedTabOrdinal = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabOrdinal) {
            0 -> MainMealScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> MonthlyCalendarScreen(
                viewModel = viewModel,
                onDayClick = { date ->
                    viewModel.setSelectedDate(date)
                    selectedTabOrdinal = 0
                },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> FavoritesAndRatingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SchoolAndSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
