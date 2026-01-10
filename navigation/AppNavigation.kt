package com.example.guitarchordmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

import com.example.guitarchordmanager.login.LoginScreen
import com.example.guitarchordmanager.songlist.SongListScreen
import com.example.guitarchordmanager.songdetail.SongDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // startDestination: 앱 켜자마자 보일 화면 이름
    NavHost(navController = navController, startDestination = "SongListScreen") {

        // LoginScreen 화면
        composable("LoginScreen") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 신호가 오면 실행될 코드
                    navController.navigate("SongListScreen") {
                        // ⭐️ 뒤로가기 눌렀을 때 로그인 화면으로 돌아가게 함. 풀고 싶으면 주석 해제
                        // popUpTo("LoginScreen") { inclusive = true }
                    }
                }
            )
        }

        // SongListScreen 화면
        composable("SongListScreen") {
            SongListScreen(
                onSongClick = { song ->
                    // SongDetailScreen으로 이동
                    navController.navigate("detail/${song.title}/${song.artist}")
                }
            )
        }

        // SongDetailScreen 화면
        composable(
            route = "detail/{title}/{artist}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("artist") { type = NavType.StringType }
            )
        ) {
            backStackEntry ->
            // 경로에서 데이터 꺼내기
            val title = backStackEntry.arguments?.getString("title") ?: "제목 없음"
            val artist = backStackEntry.arguments?.getString("artist") ?: "가수 없음"

            SongDetailScreen(
                title = title,
                artist = artist,
                onBackClick = { navController.popBackStack() } // 뒤로가기
            )
        }
    }
}
