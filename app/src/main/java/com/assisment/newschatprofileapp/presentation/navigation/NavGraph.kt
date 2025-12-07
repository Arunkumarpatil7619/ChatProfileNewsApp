package com.assisment.newschatprofileapp.presentation.navigation



import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.assisment.newschatprofileapp.presentation.home.HomeScreen
import com.assisment.newschatprofileapp.presentation.messages.MessagesScreen
import com.assisment.newschatprofileapp.presentation.profile.ProfileScreen


@Composable
fun NavGraph(navController: NavController) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen() }
        composable("messages") { MessagesScreen() }
        composable("profile") { ProfileScreen() }
    }

}

