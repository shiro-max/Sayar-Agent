package com.sayar.assistant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sayar.assistant.presentation.ui.screens.HomeScreen
import com.sayar.assistant.presentation.ui.screens.LoginScreen
import com.sayar.assistant.presentation.ui.screens.SettingsScreen
import com.sayar.assistant.presentation.ui.screens.StudentsScreen
import com.sayar.assistant.presentation.ui.screens.TimetableScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val TIMETABLE = "timetable"
    const val STUDENTS = "students"
    const val SETTINGS = "settings"
}

@Composable
fun SayarNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTimetable = { navController.navigate(Routes.TIMETABLE) },
                onNavigateToStudents = { navController.navigate(Routes.STUDENTS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TIMETABLE) {
            TimetableScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STUDENTS) {
            StudentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
