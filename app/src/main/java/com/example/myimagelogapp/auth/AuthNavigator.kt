package com.example.myimagelogapp.auth

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myimagelogapp.R

object AuthNavigator {

    fun redirectToLoginIfNeeded(fragment: Fragment): Boolean {
        if (AuthSession.isLoggedIn(fragment.requireContext())) return false

        val nav = fragment.findNavController()
        if (nav.currentDestination?.id == R.id.loginFragment) return false

        nav.navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
        return true
    }

    fun enforceAuthAtStartup(navController: NavController, context: Context) {
        if (AuthSession.isLoggedIn(context)) return

        val destinationId = navController.currentDestination?.id ?: return
        if (destinationId == R.id.loginFragment || destinationId == R.id.splashFragment) return

        navController.navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }
}
