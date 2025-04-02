package com.example.moneymind

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moneymind.pages.Login
import com.example.moneymind.pages.SignUp
import com.example.moneymind.AuthViewModel
import com.example.moneymind.pages.Savings
import com.example.moneymind.pages.Welcome

@Composable
fun NavigationController(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome", builder = {
        composable("welcome"){
            Welcome(modifier,navController,authViewModel)
        }
        composable("login"){
            Login(modifier, navController,authViewModel)
        }
        composable("signup"){
            SignUp(modifier,navController,authViewModel)
        }
        composable("savings"){
            Savings(modifier,navController,authViewModel)
        }
    })
}