package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myimagelogapp.R
import com.example.myimagelogapp.MyImageLogApp
import com.example.myimagelogapp.auth.AuthSession

/**
 * 앱 시작 시 저장된 로그인 세션 유무로 진입 화면을 분기하는 프래그먼트.
 */
class SplashFragment : Fragment(R.layout.fragment_splash) {

    /**
     * 화면 진입 즉시 로그인 상태를 확인하고 Home 또는 Login으로 이동한다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val crash = MyImageLogApp.consumeLastCrash(requireActivity().application)
        if (!crash.isNullOrBlank()) {
            Toast.makeText(requireContext(), "직전 크래시가 발생했어요. 로그캣 CrashCapture 확인!", Toast.LENGTH_LONG).show()
        }

        val nav = findNavController()
        if (AuthSession.isLoggedIn(requireContext())) {
            nav.navigate(
                R.id.action_splash_to_home,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
            )
        } else {
            nav.navigate(
                R.id.action_splash_to_login,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
            )
        }
    }
}

