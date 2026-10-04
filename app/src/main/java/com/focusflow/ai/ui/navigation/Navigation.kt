package com.focusflow.ai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusflow.ai.ui.screens.AnalyticsScreen
import com.focusflow.ai.ui.screens.CalendarScreen
import com.focusflow.ai.ui.screens.DashboardScreen
import com.focusflow.ai.ui.screens.FocusScreen
import com.focusflow.ai.ui.screens.NoteEditScreen
import com.focusflow.ai.ui.screens.NotesScreen
import com.focusflow.ai.ui.screens.ProfileScreen
import com.focusflow.ai.ui.screens.ProjectDetailScreen
import com.focusflow.ai.ui.screens.ProjectsScreen
import com.focusflow.ai.ui.screens.SearchScreen
import com.focusflow.ai.ui.screens.SettingsScreen
import com.focusflow.ai.ui.screens.TaskDetailScreen
import com.focusflow.ai.ui.screens.TaskEditScreen
import com.focusflow.ai.ui.screens.TaskListScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val TASKS = "tasks"
    const val FOCUS = "focus"
    const val CALENDAR = "calendar"
    const val ANALYTICS = "analytics"
    const val PROJECTS = "projects"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val TASK_EDIT = "task_edit/{taskId}"
    const val TASK_DETAIL = "task/{taskId}"
    const val NOTES = "notes"
    const val NOTE_EDIT = "note_edit/{noteId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"

    fun projectDetail(id: Long) = "project/$id"
    fun taskEdit(id: Long = 0L) = "task_edit/$id"
    fun taskDetail(id: Long) = "task/$id"
    fun noteEdit(id: Long = 0L) = "note_edit/$id"
}

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "Home", Icons.Rounded.Dashboard),
    BottomDestination(Routes.TASKS, "Tasks", Icons.Rounded.Checklist),
    BottomDestination(Routes.FOCUS, "Focus", Icons.Rounded.Timer),
    BottomDestination(Routes.CALENDAR, "Calendar", Icons.Rounded.DateRange),
    BottomDestination(Routes.ANALYTICS, "Stats", Icons.Rounded.BarChart)
)

@Composable
fun FocusFlowNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = bottomDestinations.map { it.route }.toSet()

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddTask = { navController.navigate(Routes.taskEdit()) },
                    onOpenTask = { id -> navController.navigate(Routes.taskDetail(id)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenProjects = { navController.navigate(Routes.PROJECTS) },
                    onOpenFocus = { navController.navigate(Routes.FOCUS) }
                )
            }
            composable(Routes.TASKS) {
                TaskListScreen(
                    onAddTask = { navController.navigate(Routes.taskEdit()) },
                    onOpenTask = { id -> navController.navigate(Routes.taskDetail(id)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) }
                )
            }
            composable(Routes.FOCUS) { FocusScreen() }
            composable(Routes.CALENDAR) {
                CalendarScreen(onOpenTask = { id -> navController.navigate(Routes.taskDetail(id)) })
            }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }

            composable(Routes.PROJECTS) {
                ProjectsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProject = { id -> navController.navigate(Routes.projectDetail(id)) }
                )
            }
            composable(
                route = Routes.PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                ProjectDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTask = { id -> navController.navigate(Routes.taskDetail(id)) }
                )
            }
            composable(
                route = Routes.TASK_EDIT,
                arguments = listOf(navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) {
                TaskEditScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.TASK_DETAIL,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) {
                TaskDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.taskEdit(id)) }
                )
            }
            composable(Routes.NOTES) {
                NotesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenNote = { id -> navController.navigate(Routes.noteEdit(id)) },
                    onAddNote = { navController.navigate(Routes.noteEdit()) }
                )
            }
            composable(
                route = Routes.NOTE_EDIT,
                arguments = listOf(navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) {
                NoteEditScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTask = { id -> navController.navigate(Routes.taskDetail(id)) },
                    onOpenProject = { id -> navController.navigate(Routes.projectDetail(id)) },
                    onOpenNote = { id -> navController.navigate(Routes.noteEdit(id)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
