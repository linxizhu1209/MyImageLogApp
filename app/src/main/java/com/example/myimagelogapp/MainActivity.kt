package com.example.myimagelogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.myimagelogapp.auth.AuthSession

class MainActivity : AppCompatActivity() {

    /**
     * 같은 딥링크가 중복으로 들어오는 경우(특히 카카오) 한 번만 처리하기 위한 키.
     */
    private var lastHandledAuthUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lastHandledAuthUrl = savedInstanceState?.getString(KEY_LAST_AUTH_URL)
        handleAuthDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme != "myimagelogapp" || data.host != "auth") return

        val authUrl = data.toString()
        if (authUrl == lastHandledAuthUrl) return
        lastHandledAuthUrl = authUrl

        val token = data.getQueryParameter("token")
        val userId = data.getQueryParameter("userId")?.toLongOrNull()

        if (!token.isNullOrBlank() && userId != null && userId > 0) {
            AuthSession.save(this, token, userId)
            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
            navigateHomeAfterLogin()
        } else {
            Toast.makeText(this, "로그인 정보 파싱 실패", Toast.LENGTH_SHORT).show()
        }

        // 같은 Intent로 딥링크가 재처리되는 것을 막는다.
        intent.data = null
    }

    private fun navigateHomeAfterLogin() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment ?: return
        val nav = navHost.navController

        Handler(Looper.getMainLooper()).post {
            if (nav.currentDestination?.id == R.id.homeFragment) return@post

            val popTo = when (nav.currentDestination?.id) {
                R.id.loginFragment -> R.id.loginFragment
                R.id.splashFragment -> R.id.splashFragment
                else -> R.id.nav_graph
            }

            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(popTo, true)
                .build()

            runCatching { nav.navigate(R.id.homeFragment, null, options) }
        }
    }

    /**
     * 딥링크 중복 처리 방지를 위해 마지막 처리 URL을 저장한다.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_LAST_AUTH_URL, lastHandledAuthUrl)
    }

    private companion object {
        private const val KEY_LAST_AUTH_URL = "last_auth_url"
    }
}